/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.launcher

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import spock.lang.IgnoreIf
import static org.gradle.integtests.fixtures.GradleDistributionExecuter.getSystemPropertyExecuter

/**
 * Verifies that Gradle doesn't pollute the system class loader.
 *
 * This is important for plugins that need to use isolated class loaders to avoid conflicts.
 *
 * When running without the daemon, success is dependant on the start scripts doing the right thing.
 * When running with the daemon, success is dependent on DaemonConnector forking the daemon process with the right classpath.
 *
 * This test is not meaningful when running the embedded integration test mode, so we ignore it in that case.
 */
class SystemClassLoaderTest extends AbstractIntegrationSpec {

    static heading = "systemClassLoader info"
    static noInfoHeading = "no systemClassLoader info"

    @IgnoreIf({ !getSystemPropertyExecuter().forks })
    def "daemon bootstrap classpath is bare bones"() {
        given:
        buildFile << """
            task loadClasses << {
                def systemLoader = ClassLoader.systemClassLoader

                systemLoader.loadClass(org.gradle.launcher.GradleMain.name) // this should be on the classpath, it's from the launcher package

                def nonLauncherOrCoreClass = org.slf4j.Logger
                getClass().classLoader.loadClass(nonLauncherOrCoreClass.name)
                try {
                    systemLoader.loadClass(nonLauncherOrCoreClass.name)
                    assert false : "ClassNotFoundException should have been thrown trying to load a “core” class from the system classloader"
                } catch (ClassNotFoundException e) {
                    //
                }

                if (systemLoader instanceof java.net.URLClassLoader) {
                    def systemLoaderUrls = systemLoader.URLs
                    println "$heading"
                    println systemLoaderUrls.size()
                    println systemLoaderUrls[0]
                } else {
                    println "$noInfoHeading"
                }
            }
        """

        when:
        succeeds "loadClasses"

        then:
        def lines = output.readLines()
        if (lines.find { it == noInfoHeading }) { return }

        lines.find { it == heading } // here for nicer output if the output isn't what we expect
        def headingIndex = lines.indexOf(heading)
        lines[headingIndex + 1] == "1"
        lines[headingIndex + 2].contains("gradle-launcher")
    }
}