/**
 * 
 */
package org.beedom.dslforge

import java.lang.reflect.Method;

import groovy.util.logging.Slf4j


/**
 * @author kovax
 *
 */
@Slf4j
class SimpleRenderer implements ReportRenderer {
    
    public enum ReportType {TXT, HTML, XML, TEXTILE, MARKKDOWN, TWIKI, MEDIAWIKI, CONFLUENCE, TRACWIKI}

    private ReportType type
    private PrintWriter writer

    private boolean numbered = false    
    private static final int tabSize = 2

    private int level = 0

    /**
     * 
     * @param w
     * @param t
     */
    public SimpleRenderer(Writer w, ReportType t) {
        writer = w
        type = t
    }


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
    public void openContext(String clazz, String context, String desc) {
        log.debug "$type rendering(open  level $level): '$clazz: $context $desc'"

        if(type == ReportType.XML) {
            printTabs(level)
            writer.println "<${clazz} context='$context' description='$desc'>"
        }
        else {
            //Add new line at the very beginning for TXT report
            if(type == ReportType.TXT && level > 0) { writer.println() }

            writeAllExceptXML(clazz, context, desc )

            //Add new line after each context opened for TXT report
            if(type == ReportType.TXT) { writer.println() }
        }

        level++
    }


    /**
     * 
     */
    public void closeContext(String clazz, String context) {
        level--
        log.debug "$type rendering(close level $level): '$clazz: $context'"
        
        if(type == ReportType.XML) {
            printTabs(level)
            writer.println "</${clazz}>"
        }
        else if(type == ReportType.TXT) {
            writer.println()
        }
    }


    /**
     * 
     */
    public void writeMethod(String clazz, String method, String desc ) {
        log.debug "$type rendering(mehod level $level): '$clazz: $method $desc'"
        
        if(type == ReportType.XML) {
            printTabs(level)
            writer.println "<${method}>$desc</${method}>"
        }
        else {
            writeAllExceptXML(clazz, method, desc )
        }
    }


    /**
     * 
     * @param clazz
     * @param method
     * @param desc
     */
    private void writeAllExceptXML(String clazz, String method, String desc ) {
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
