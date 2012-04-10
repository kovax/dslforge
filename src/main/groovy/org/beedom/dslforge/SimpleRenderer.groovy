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

    
    public SimpleRenderer(Writer w, ReportType t) {
        writer = w
        type = t
    }


    private void printTabs(int level) {
        (level*tabSize).times { writer.print(" ") }
    }

  
    public static String getFileExt(ReportType t) {
        return t.toString().toLowerCase()
    }


    public void openContext(String clazz, String context, String desc) {
        log.debug "$type rendering(open  level $level): '$clazz: $context $desc'"

        if(type == ReportType.XML) {
            printTabs(level)
            writer.println "<${clazz} context='$context' description='$desc'>"
        }
        else {
            if(type == ReportType.TXT && level > 0) { writer.println() }

            writeIt(clazz, context, desc )

            if(type == ReportType.TXT) { writer.println() }
        }

        level++
    }


    public void closeContext(String clazz, String context) {
        level--
        log.debug "$type rendering(close level $level): '$clazz: $context'"
        
        if(type == ReportType.XML) {
            printTabs(level)
            writer.println "</${clazz}>"
        }
        else if(type == ReportType.TXT) {
            writer.println ""
        }
    }
    

    public void writeMethod(String clazz, String method, String desc ) {
        log.debug "$type rendering(mehod level $level): '$clazz: $method $desc'"
        
        if(type == ReportType.XML) {
            printTabs(level)
            writer.println "<${method}>$desc</${method}>"
        }
        else {
            writeIt(clazz, method, desc )
        }
    }


    private void writeIt(String clazz, String method, String desc ) {
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
            printTabs(level)
            writer.println "$method $desc"
        }
    }
}
