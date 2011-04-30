/*
* Copyright 2003-2011 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.beedom.dslforge


/**
 * 
 *
 * @author zs.myth
 *
 */
public class BindingConvention {

    /**
     * Convention: 
     */
    private static final defaultBeanKeys = ["id","key"]
                          
    /**
     * 
     */
    private static final removedChars = "[\\s,./:!?;\$]+"

        
    /**
     * 
     * 
     * @param name
     * @return
     */
    static String convertToPropertyName(String name) {
        return (name.substring(0,1).toLowerCase() + name.substring(1)).replaceAll(removedChars, "")
    }
    
    
    /**
     * Retrieves or converts property values to property name used in Binding
     * 
     * @param key either String to identify bean property or Closure to convert values to property name
     * @param obj the Bean object to be bound
     * @return
     */
    static String getUserDefinedPropName(key, obj) {
        assert key, "specify object to identify key of type String or Closure"
        
        if(key instanceof String ) {
            return convertToPropertyName( obj."$key" )
        }
        else if(key instanceof Closure) {
            return convertToPropertyName( key(obj) )
        }
        else {
            //TODO: replace this with DSL specific exception
            assert false, "user defined key must be String or Closure - unhandled class: ${key.class.simpleName}"
        }
    }
    
    /**
     * Convention: 
     * 
     * @param obj 
     * @return
     */
    static String getDefaultPropName(obj) {
        for(key in defaultBeanKeys) {
            if(obj.hasProperty(key) && obj."$key") {
                return convertToPropertyName( obj.class.simpleName + obj."$key" )
            }
        }
        return null
    }

    /**
     * 
     * 
     * @param binding
     * @param obj
     * @return
     */
    static def bindObject(binding, obj) {
        bindObject(binding, obj, null)
    }

    /**
     * 
     * @param binding
     * @param obj
     * @param key
     * @return
     */
    static def bindObject(binding, obj, key) {
        assert binding

        def propName
        
        //lookup the user defined key for the current bean
        if(key) {
            propName = getUserDefinedPropName(key, obj)
        }
        
        //if none was given, try to use the default keys
        if(!propName) {
            propName = getDefaultPropName(obj)
        }
        
        assert propName, "object must have id/key or specify property name in configuration"
        
        binding."$propName" = obj
    }

    /**
     * 
     * @param binding
     * @param objs
     * @param beanKeys
     * @return
     */
    static def bindObjectList(binding, objects, beanKeys) {
        assert binding
        
        for (o in objects) {
            bindObject(binding, o, beanKeys[o.class])
        }
    }
}
