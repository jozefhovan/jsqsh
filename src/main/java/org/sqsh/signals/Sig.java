
/*
 * Copyright 2007-2012 Scott C. Gray
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
package org.sqsh.signals;

/**
 * Interface that represents a signal. This is supposed to abstract the
 * JVM's specific implementation of the signal.
 */
public class Sig {
    
    private String name;
    
    public Sig (String name) {
        
        this.name = name;
    }
    
    /**
     * Returns the name of the signal that was received.
     * @return The name of the signal that was received.
     */
    public String getName() {
        
        return name;
    }
}
