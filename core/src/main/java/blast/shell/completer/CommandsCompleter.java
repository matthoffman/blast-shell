/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package blast.shell.completer;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import blast.shell.Completer;
import blast.shell.Completer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

public class CommandsCompleter implements Completer, BeanFactoryAware {

    private final Map<String, Completer> completers = new ConcurrentHashMap<String, Completer>();
    private ListableBeanFactory beanFactory;

    public void register() {
//        beanFactory.getBeansOfType(arg0)
//        Set<String> functions = getNames(reference);
//        if (functions != null) {
//            List<Completer> cl = new ArrayList<Completer>();
//            cl.add(new StringsCompleter(functions));
//            try {
//                Object function = bundleContext.getService(reference);
//                if (function instanceof Completable) {
//                    List<Completer> fcl = ((Completable) function).getCompleters();
//                    if (fcl != null) {
//                        for (Completer c : fcl) {
//                            cl.add(c == null ? NullCompleter.INSTANCE : c);
//                        }
//                    } else {
//                        cl.add(NullCompleter.INSTANCE);
//                    }
//                } else {
//                    cl.add(NullCompleter.INSTANCE);
//                }
//            } finally {
//                bundleContext.ungetService(reference);
//            }
//            ArgumentCompleter c = new ArgumentCompleter(cl);
//            completers.put(reference, c);
//        }
    }

//    public void unregister(ServiceReference reference) {
//        if (reference != null) {
//            completers.remove(reference);
//        }
//    }
//
//    private Set<String> getNames(ServiceReference reference) {
//        Set<String> names = new HashSet<String>();
//        Object scope = reference.getProperty(CommandProcessor.COMMAND_SCOPE);
//        Object function = reference.getProperty(CommandProcessor.COMMAND_FUNCTION);
//        if(scope != null && function != null)
//        {
//            if (function.getClass().isArray())
//            {

    //                for (Object f : ((Object[]) function))
//                {
//                    names.add(scope + ":" + f.toString());
//                }
//            }
//            else
//            {
//                names.add(scope + ":" + function.toString());
//            }
//            return names;
//        }
//        return null;
//    }
//
    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        int res = new AggregateCompleter(completers.values()).complete(buffer, cursor, candidates);
        Collections.sort(candidates);
        return res;
    }

    //
    @Override
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.beanFactory = (ListableBeanFactory) factory;
    }

}

