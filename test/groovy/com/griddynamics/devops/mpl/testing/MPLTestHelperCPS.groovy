//
// Copyright (c) 2019 Grid Dynamics International, Inc. All Rights Reserved
// https://www.griddynamics.com
//
// Classification level: Public
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// $Id: $
// @Project:     MPL
// @Description: Shared Jenkins Modular Pipeline Library
//

package com.griddynamics.devops.mpl.testing

import com.lesfurets.jenkins.unit.cps.PipelineTestHelperCPS
import com.lesfurets.jenkins.unit.MethodSignature

import com.cloudbees.groovy.cps.impl.CpsCallableInvocation
import com.cloudbees.groovy.cps.Envs
import com.cloudbees.groovy.cps.Continuation

@groovy.transform.InheritConstructors
class MPLTestHelperCPS extends PipelineTestHelperCPS {
  public getLibraryConfig() {
    gse.getConfig()
  }
  public getLibraryClassLoader() {
    gse.groovyClassLoader
  }
  
  void registerAllowedMethod(MethodSignature methodSignature, Closure closure) {
    if( isMethodAllowed(methodSignature.name, methodSignature.args) )
      return // Skipping methods already existing in the list
    allowedMethodCallbacks.put(methodSignature, closure)
  }

  // To make NotSerializableException more readable
  @Override
  Object callClosure(Closure closure, Object[] args = null) {
      try {
          callClosure2(closure, args)
      } catch(CpsCallableInvocation e) {
          def next = e.invoke(Envs.empty(), null, Continuation.HALT)
          while(next.yield==null) {
              try {
                  this.roundtripSerialization(next.e)
              } catch (exception) {
                  throw new Exception("Exception during serialization in `${next.e.closureOwner().class.name}` for class ${exception.getMessage()}", exception)
              }
              next = next.step()
          }
          return next.yield.replay()
      }
  }

  Object callClosure2(Closure closure, Object[] args = null) {
      if (!args) {
          return closure.call()
      } else if (args.size() > closure.maximumNumberOfParameters) {
          return closure.call(args)
      } else {
          return closure.call(*args)
      }
  }
}
