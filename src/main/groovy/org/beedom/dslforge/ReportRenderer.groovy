/**
 * 
 */
package org.beedom.dslforge

/**
 * 
 * @author kovax
 *
 */
interface ReportRenderer {
    
    /**
     * 
     * @param dslKey
     * @param alias
     * @param desc
     */
    public void openContext(String dslKey, String alias, String desc);

    
    /**
     * 
     * @param dslKey
     * @param alias
     * @param method
     * @param desc
     */
    public void writeMethod(String dslKey, String method, String alias, String desc);


    /**
     * 
     * @param dslKey
     * @param alias
     */
    public void closeContext(String dslKey, String alias);
}
