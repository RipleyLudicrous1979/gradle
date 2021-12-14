/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.plugins.ide.eclipse

import org.gradle.plugins.ide.eclipse.internal.EclipsePluginConstants

class EclipseTestSourcesIntegrationSpec extends AbstractEclipseIntegrationSpec {

    def "classpath contains test flags for project dependencies coming from test scopes(:a[sourceSet=src/#fromSourceSet/java] --(#dependency)--> :b[plugin=#toProjectType,sourceSet=#toSourceSet])"() {
        when:
        settingsFile << """
            include 'a', 'b'
        """
        file('a/build.gradle') << """
            plugins {
                id 'java-library'
                id 'eclipse'
            }

            dependencies {
                $dependency(project(":b"))
            }
        """
        file("a/src/$fromSourceSet/java/A.java") << """
            public class A {
                public B b;
            }
        """
        file('b/build.gradle') << """
            plugins {
                id 'java-library'
                id 'eclipse'
                ${toProjectType == 'java-test-fixtures' ? "id 'java-test-fixtures'" : "" }
            }
        """
        file("b/src/$toSourceSet/java/B.java") << """
            public class B {
            }
        """

        then:
        if (expectedBuildOutcome in ['OK', 'compilation-failure']) {
            run 'eclipse'
            expectedDependency(classpath('a').projects.find {it.name == 'b' })
        }

        and:
        if (expectedBuildOutcome == 'OK') {
            run 'build'
        } else if (expectedBuildOutcome == 'compilation-failure') {
            runAndFail('build').assertHasCause('Compilation failed; see the compiler error output for details')
        } else if (expectedBuildOutcome == 'invalid-configuration') {
            runAndFail('build').assertHasCause('Could not resolve all task dependencies for configuration')
        } else {
            throw new RuntimeException("Invalid expected result: $expectedBuildOutcome")
        }

        where:
        fromSourceSet  | dependency                         | toProjectType        | toSourceSet    | expectedBuildOutcome    | expectedDependency
        'main'         | 'implementation'                   | 'java-library'       | 'main'         | 'OK'                    | DEPENDENCY_WITHOUT_TEST_SOURCES
        'main'         | 'implementation'                   | 'java-library'       | 'test'         | 'compilation-failure'   | DEPENDENCY_WITHOUT_TEST_SOURCES
        'main'         | 'implementation'                   | 'java-library'       | 'testFixtures' | 'compilation-failure'   | DEPENDENCY_WITHOUT_TEST_SOURCES
        'main'         | 'implementation'                   | 'java-test-fixtures' | 'main'         | 'OK'                    | DEPENDENCY_WITH_TEST_SOURCES
        'main'         | 'implementation'                   | 'java-test-fixtures' | 'test'         | 'compilation-failure'   | DEPENDENCY_WITH_TEST_SOURCES
        'main'         | 'implementation'                   | 'java-test-fixtures' | 'testFixtures' | 'compilation-failure'   | DEPENDENCY_WITH_TEST_SOURCES
        'main'         | 'testImplementation'               | 'java-library'       | 'main'         | 'compilation-failure'   | TEST_DEPENDENCY_WITHOUT_TEST_SOURCES
        'main'         | 'testImplementation'               | 'java-library'       | 'test'         | 'compilation-failure'   | TEST_DEPENDENCY_WITHOUT_TEST_SOURCES
        'main'         | 'testImplementation'               | 'java-library'       | 'testFixtures' | 'compilation-failure'   | TEST_DEPENDENCY_WITHOUT_TEST_SOURCES
        'main'         | 'testImplementation'               | 'java-test-fixtures' | 'main'         | 'compilation-failure'   | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'main'         | 'testImplementation'               | 'java-test-fixtures' | 'test'         | 'compilation-failure'   | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'main'         | 'testImplementation'               | 'java-test-fixtures' | 'testFixtures' | 'compilation-failure'   | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'main'         | 'testImplementation testFixtures'  | 'java-library'       | 'main'         | 'invalid-configuration' | null
        'main'         | 'testImplementation testFixtures'  | 'java-library'       | 'test'         | 'invalid-configuration' | null
        'main'         | 'testImplementation testFixtures'  | 'java-library'       | 'testFixtures' | 'invalid-configuration' | null
        'main'         | 'testImplementation testFixtures'  | 'java-test-fixtures' | 'main'         | 'compilation-failure'   | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'main'         | 'testImplementation testFixtures'  | 'java-test-fixtures' | 'test'         | 'compilation-failure'   | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'main'         | 'testImplementation testFixtures'  | 'java-test-fixtures' | 'testFixtures' | 'compilation-failure'   | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'test'         | 'implementation'                   | 'java-library'       | 'main'         | 'OK'                    | DEPENDENCY_WITHOUT_TEST_SOURCES
        'test'         | 'implementation'                   | 'java-library'       | 'test'         | 'compilation-failure'   | DEPENDENCY_WITHOUT_TEST_SOURCES
        'test'         | 'implementation'                   | 'java-library'       | 'testFixtures' | 'compilation-failure'   | DEPENDENCY_WITHOUT_TEST_SOURCES
        'test'         | 'implementation'                   | 'java-test-fixtures' | 'main'         | 'OK'                    | DEPENDENCY_WITH_TEST_SOURCES
        'test'         | 'implementation'                   | 'java-test-fixtures' | 'test'         | 'compilation-failure'   | DEPENDENCY_WITH_TEST_SOURCES // no compilation failure in Eclipse
        'test'         | 'implementation'                   | 'java-test-fixtures' | 'testFixtures' | 'compilation-failure'   | DEPENDENCY_WITH_TEST_SOURCES // no compilation failure in Eclipse
        'test'         | 'testImplementation'               | 'java-library'       | 'main'         | 'OK'                    | TEST_DEPENDENCY_WITHOUT_TEST_SOURCES
        'test'         | 'testImplementation'               | 'java-library'       | 'test'         | 'compilation-failure'   | TEST_DEPENDENCY_WITHOUT_TEST_SOURCES
        'test'         | 'testImplementation'               | 'java-library'       | 'testFixtures' | 'compilation-failure'   | TEST_DEPENDENCY_WITHOUT_TEST_SOURCES
        'test'         | 'testImplementation'               | 'java-test-fixtures' | 'main'         | 'OK'                    | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'test'         | 'testImplementation'               | 'java-test-fixtures' | 'test'         | 'compilation-failure'   | TEST_DEPENDENCY_WITH_TEST_SOURCES // no compilation failure in Eclipse
        'test'         | 'testImplementation'               | 'java-test-fixtures' | 'testFixtures' | 'compilation-failure'   | TEST_DEPENDENCY_WITH_TEST_SOURCES // no compilation failure in Eclipse
        'test'         | 'testImplementation testFixtures'  | 'java-library'       | 'main'         | 'invalid-configuration' | null
        'test'         | 'testImplementation testFixtures'  | 'java-library'       | 'test'         | 'invalid-configuration' | null
        'test'         | 'testImplementation testFixtures'  | 'java-library'       | 'testFixtures' | 'invalid-configuration' | null
        'test'         | 'testImplementation testFixtures'  | 'java-test-fixtures' | 'main'         | 'OK'                    | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'test'         | 'testImplementation testFixtures'  | 'java-test-fixtures' | 'test'         | 'compilation-failure'   | TEST_DEPENDENCY_WITH_TEST_SOURCES // no compilation failure in Eclipse
        'test'         | 'testImplementation testFixtures'  | 'java-test-fixtures' | 'testFixtures' | 'OK'                    | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'testFixtures' | 'implementation'                   | 'java-library'       | 'main'         | 'OK'                    | DEPENDENCY_WITHOUT_TEST_SOURCES
        'testFixtures' | 'implementation'                   | 'java-library'       | 'test'         | 'OK'                    | DEPENDENCY_WITHOUT_TEST_SOURCES
        'testFixtures' | 'implementation'                   | 'java-library'       | 'testFixtures' | 'OK'                    | DEPENDENCY_WITHOUT_TEST_SOURCES
        'testFixtures' | 'implementation'                   | 'java-test-fixtures' | 'main'         | 'OK'                    | DEPENDENCY_WITH_TEST_SOURCES
        'testFixtures' | 'implementation'                   | 'java-test-fixtures' | 'test'         | 'OK'                    | DEPENDENCY_WITH_TEST_SOURCES
        'testFixtures' | 'implementation'                   | 'java-test-fixtures' | 'testFixtures' | 'OK'                    | DEPENDENCY_WITH_TEST_SOURCES
        'testFixtures' | 'testImplementation'               | 'java-library'       | 'main'         | 'OK'                    | TEST_DEPENDENCY_WITHOUT_TEST_SOURCES
        'testFixtures' | 'testImplementation'               | 'java-library'       | 'test'         | 'OK'                    | TEST_DEPENDENCY_WITHOUT_TEST_SOURCES
        'testFixtures' | 'testImplementation'               | 'java-library'       | 'testFixtures' | 'OK'                    | TEST_DEPENDENCY_WITHOUT_TEST_SOURCES
        'testFixtures' | 'testImplementation'               | 'java-test-fixtures' | 'main'         | 'OK'                    | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'testFixtures' | 'testImplementation'               | 'java-test-fixtures' | 'test'         | 'OK'                    | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'testFixtures' | 'testImplementation'               | 'java-test-fixtures' | 'testFixtures' | 'OK'                    | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'testFixtures' | 'testImplementation testFixtures'  | 'java-library'       | 'main'         | 'invalid-configuration' | null
        'testFixtures' | 'testImplementation testFixtures'  | 'java-library'       | 'test'         | 'invalid-configuration' | null
        'testFixtures' | 'testImplementation testFixtures'  | 'java-library'       | 'testFixtures' | 'invalid-configuration' | null
        'testFixtures' | 'testImplementation testFixtures'  | 'java-test-fixtures' | 'main'         | 'OK'                    | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'testFixtures' | 'testImplementation testFixtures'  | 'java-test-fixtures' | 'test'         | 'OK'                    | TEST_DEPENDENCY_WITH_TEST_SOURCES
        'testFixtures' | 'testImplementation testFixtures'  | 'java-test-fixtures' | 'testFixtures' | 'OK'                    | TEST_DEPENDENCY_WITH_TEST_SOURCES
    }

    private static final Closure DEPENDENCY_WITH_TEST_SOURCES = { EclipseClasspathFixture.EclipseProjectDependency d ->
        d.assertHasNoAttribute(EclipsePluginConstants.TEST_SOURCES_ATTRIBUTE_KEY, EclipsePluginConstants.TEST_SOURCES_ATTRIBUTE_VALUE)
        d.assertHasAttribute(EclipsePluginConstants.WITHOUT_TEST_CODE_ATTRIBUTE_KEY, 'false')
    }

    private static final Closure DEPENDENCY_WITHOUT_TEST_SOURCES = { EclipseClasspathFixture.EclipseProjectDependency d ->
        d.assertHasNoAttribute(EclipsePluginConstants.TEST_SOURCES_ATTRIBUTE_KEY, EclipsePluginConstants.TEST_SOURCES_ATTRIBUTE_VALUE)
        d.assertHasNoAttribute(EclipsePluginConstants.WITHOUT_TEST_CODE_ATTRIBUTE_KEY, 'false')
    }

    private static final Closure TEST_DEPENDENCY_WITH_TEST_SOURCES = { EclipseClasspathFixture.EclipseProjectDependency d ->
        d.assertHasAttribute(EclipsePluginConstants.TEST_SOURCES_ATTRIBUTE_KEY, EclipsePluginConstants.TEST_SOURCES_ATTRIBUTE_VALUE)
        d.assertHasAttribute(EclipsePluginConstants.WITHOUT_TEST_CODE_ATTRIBUTE_KEY, 'false')
    }

    private static final Closure TEST_DEPENDENCY_WITHOUT_TEST_SOURCES = { EclipseClasspathFixture.EclipseProjectDependency d ->
        d.assertHasAttribute(EclipsePluginConstants.TEST_SOURCES_ATTRIBUTE_KEY, EclipsePluginConstants.TEST_SOURCES_ATTRIBUTE_VALUE)
        d.assertHasNoAttribute(EclipsePluginConstants.WITHOUT_TEST_CODE_ATTRIBUTE_KEY, 'false')
    }
}
