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
     * @param clazz
     * @param context
     * @param desc
     */
    public void openContext(String clazz, String context, String desc);

    
    /**
     * 
     * @param clazz
     * @param method
     * @param desc
     */
    public void writeMethod(String clazz, String method, String desc);


    /**
     * 
     * @param clazz
     * @param context
     */
    public void closeContext(String clazz, String context);
}
