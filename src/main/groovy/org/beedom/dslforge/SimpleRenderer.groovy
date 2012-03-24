/**
 * 
 */
package org.beedom.dslforge

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
    private int prevIndentLevel = -1
    private boolean numbered = false

    public SimpleRenderer(Writer w, ReportType t) {
        writer = w
        type = t
    }
    
    public static String getFileExt(ReportType t) {
        return t.toString().toLowerCase()
    }

    public void write(int level, String method, String desc ) {
        if(type == ReportType.HTML) {
            log.debug "HTML rendering: '$method $desc'"
            
            writer.println "<h${level+2}>$method $desc</h${level+2}> "
        }
        else if(type == ReportType.XML) {
            log.debug "XML rendering: '$method $desc'"
            
        }
        else if(type == ReportType.TEXTILE) {
            log.debug "TEXTILE rendering: '$method $desc'"
            
        }
        else if(type == ReportType.MARKKDOWN) {
            log.debug "MARKKDOWN rendering: '$method $desc'"
            
        }
        else if(type == ReportType.TWIKI) {
            log.debug "TWIKI rendering: '$method $desc'"
            
        }
        else if(type == ReportType.MEDIAWIKI) {
            log.debug "MEDIAWIKI rendering: '$method $desc'"
            
        }
        else if(type == ReportType.CONFLUENCE) {
            log.debug "CONFLUENCE rendering: '$method $desc'"
            
        }
        else if(type == ReportType.TRACWIKI) {
            log.debug "TRACWIKI rendering: '$method $desc'"
            
        }
        else {
            log.debug "TEXT rendering: '$method $desc'"

            level.times { writer.print("    ") }
            writer.println "$method $desc"
        }
        
        prevIndentLevel = level
    }
}
