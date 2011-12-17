/*
 *      Copyright 2008 the original author or authors
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

package groovytools.builder;

/**
 * Thrown when accessing or setting a property.
 *
 * @author didge
 * @version $Id: PropertyException.java 35 2008-08-29 20:59:09Z didge $
 */
public class PropertyException extends MetaBuilderException {
    public PropertyException() {
    }

    public PropertyException(String message) {
        super(message);
    }

    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyException(Throwable cause) {
        super(cause);
    }
}