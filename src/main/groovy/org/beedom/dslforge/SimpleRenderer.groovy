/**
 * 
 */
package org.beedom.dslforge

import java.lang.reflect.Method;

import org.codehaus.groovy.transform.TupleConstructorASTTransformation;

import groovy.transform.TupleConstructor;
import groovy.util.logging.Slf4j


/**
 * @author kovax
 *
 */
@Slf4j
@TupleConstructor
class SimpleRenderer implements ReportRenderer {
    
    public enum ReportType {TXT, HTML, XML, TEXTILE, MARKKDOWN, TWIKI, MEDIAWIKI, CONFLUENCE, TRACWIKI}

    private ReportType type
    private PrintWriter writer

    private boolean numbered = false    
    private static final int tabSize = 2

    private int level = 0

    /**
     * 
     * @param t
     * @return
     */
    public static String getFileExt(ReportType t) {
        return t.toString().toLowerCase()
    }


    /**
     * 
     * @param level
     */
    private void printTabs(int level) {
        //TODO: is this the best way to print tabs????
        (level*tabSize).times { writer.print(" ") }
    }
  

    /**
     * 
     */
    public void openContext(String dslKey, String alias, String desc) {
        log.debug "$type rendering(open  level $level): '$dslKey: $alias $desc'"

        if(type == ReportType.XML) {
            printTabs(level)
            writer.print "<context dslKey='${dslKey}'"

            if(alias) { writer.println " alias='$alias'>" }
            else      { writer.println ">" }

            if(desc) {
                printTabs(level+1)
                writer.println "<description>$desc</description>"
            }
        }
        else {
            //Add new line at the very beginning for TXT report
            if(type == ReportType.TXT && level > 0) { writer.println() }

            writeAllExceptXML(dslKey, alias, desc )

            //Add new line after each context opened for TXT report
            if(type == ReportType.TXT) { writer.println() }
        }

        level++
    }


    /**
     * 
     */
    public void closeContext(String dslKey, String alias) {
        level--
        log.debug "$type rendering(close level $level): '$dslKey: $alias'"
        
        if(type == ReportType.XML) {
            printTabs(level)
            writer.println "</context>"
        }
        else if(type == ReportType.TXT) {
            writer.println()
        }
    }


    /**
     * 
     */
    public void writeMethod(String dslKey, String method, String alias, String desc) {
        log.debug "$type rendering(mehod level $level): '$dslKey/$alias: $method $desc'"
        
        if(type == ReportType.XML) {
            printTabs(level)
            writer.print "<method dslKey='$dslKey' name=$method"

            if(alias) { writer.println " alias='$alias'>" }
            else      { writer.println ">" }

            if(desc) {
                printTabs(level+1)
                writer.println "<description>$desc</description>"
            }

            printTabs(level)
            writer.println "</method>"
        }
        else {
            writeAllExceptXML(dslKey, method, desc )
        }
    }


    /**
     * 
     * @param dslKey
     * @param method
     * @param desc
     */
    private void writeAllExceptXML(String dslKey, String method, String desc ) {
        if(type == ReportType.HTML) {
            writer.println "<h${level+2}>$method $desc</h${level+2}> "
        }
        else if(type == ReportType.TEXTILE) {
            log.error "NOT IMPLEMENTED"
        }
        else if(type == ReportType.MARKKDOWN) {
            log.error "NOT IMPLEMENTED"
        }
        else if(type == ReportType.TWIKI) {
            log.error "NOT IMPLEMENTED"
        }
        else if(type == ReportType.MEDIAWIKI) {
            log.error "NOT IMPLEMENTED"
        }
        else if(type == ReportType.CONFLUENCE) {
            log.error "NOT IMPLEMENTED"
        }
        else if(type == ReportType.TRACWIKI) {
            log.error "NOT IMPLEMENTED"
        }
        else {
            //
            printTabs(level)
            writer.println "$method $desc"
        }
    }
}
