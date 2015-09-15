/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.performance

import org.junit.experimental.categories.Category
import spock.lang.Unroll

import static org.gradle.performance.measure.Duration.millis

@Category(ManualPerformanceTest)
class NewJavaPluginPerformanceTest extends AbstractCrossVersionPerformanceTest {
    @Unroll("Project '#testProject' build")
    def "build new java project"() {
        given:
        runner.testId = "build new java project $testProject"
        runner.testProject = testProject
        runner.tasksToRun = ['build']
        runner.maxExecutionTimeRegression = maxExecutionTimeRegression
        runner.targetVersions = ['2.6', 'last']
        runner.useDaemon = true
        runner.gradleOpts = ["-Xmx1g", "-XX:MaxPermSize=256m", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/tmp"]

        when:
        def result = runner.run()

        then:
        result.assertCurrentVersionHasNotRegressed()

        where:
        testProject              | maxExecutionTimeRegression
        "bigNewMultiprojectJava" | millis(5000)
    }
}
