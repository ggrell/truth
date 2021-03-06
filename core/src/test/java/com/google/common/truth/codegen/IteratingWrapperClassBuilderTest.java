/*
 * Copyright (c) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.truth.codegen;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Joiner;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for the IteratingWrapperClassBuilder
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class IteratingWrapperClassBuilderTest {
  private static final Joiner NEW_LINE_JOINER = Joiner.on("\n");

  private static final String TOP_BOILERPLATE =
      NEW_LINE_JOINER.join(
          "package com.google.common.truth.codegen;",
          "",
          "import com.google.common.truth.FailureStrategy;",
          "import com.google.common.truth.SubjectFactory;",
          "");

  private static final String SUBJECT_FACTORY_FIELD =
      "private final SubjectFactory subjectFactory;";

  private static final String ITERABLE_FIELD = "private final Iterable<%s> data;";

  private static final String CONSTRUCTOR =
      NEW_LINE_JOINER.join(
          "  public %1$sSubjectIteratingWrapper(",
          "      FailureStrategy failureStrategy,",
          "      SubjectFactory<?, ?> subjectFactory,",
          "      Iterable<%2$s> data",
          "  ) {",
          "    super(failureStrategy, (%2$s)null);",
          "    this.subjectFactory = subjectFactory;",
          "    this.data = data;",
          "  }");

  private static final String CLASS_DECLARATION =
      "public class %1$sSubjectIteratingWrapper extends %1$sSubject {";

  private static final String FOO_WRAPPED_METHOD =
      NEW_LINE_JOINER.join(
          "  @Override public void endsWith(java.lang.String arg0) {",
          "    for (java.lang.String item : data) {",
          "      com.google.common.truth.codegen.IteratingWrapperClassBuilderTest.FooSubject subject = (com.google.common.truth.codegen.IteratingWrapperClassBuilderTest.FooSubject)subjectFactory.getSubject(failureStrategy, item);",
          "      subject.endsWith(arg0);",
          "    }",
          "  }");

  private static final String BAR_WRAPPED_METHOD =
      NEW_LINE_JOINER.join(
          "  @Override public void startsWith(@javax.annotation.Nullable java.lang.String arg0) {",
          "    for (java.lang.String item : data) {",
          "      com.google.common.truth.codegen.BarSubject subject = (com.google.common.truth.codegen.BarSubject)subjectFactory.getSubject(failureStrategy, item);",
          "      subject.startsWith(arg0);",
          "    }",
          "  }");

  @Test
  public void testSubjectWrapperGeneration_PlainClass() {
    IteratingWrapperClassBuilder builder = new IteratingWrapperClassBuilder(BarSubject.BAR);
    String code = builder.build().toString();
    assertThat(code).contains(TOP_BOILERPLATE);
    assertThat(code).contains(SUBJECT_FACTORY_FIELD);
    assertThat(code).contains(String.format(ITERABLE_FIELD, "java.lang.String"));
    assertThat(code).contains(String.format(CONSTRUCTOR, "Bar", "java.lang.String"));
    assertThat(code).contains(String.format(CLASS_DECLARATION, "Bar"));
    assertThat(code).contains(BAR_WRAPPED_METHOD);
  }

  @Test
  public void testSubjectWrapperGeneration_InnerClass() {
    IteratingWrapperClassBuilder builder = new IteratingWrapperClassBuilder(FooSubject.FOO);
    String code = builder.build().toString();
    assertThat(code).contains(TOP_BOILERPLATE);
    assertThat(code).contains(SUBJECT_FACTORY_FIELD);
    assertThat(code).contains(String.format(ITERABLE_FIELD, "java.lang.String"));
    assertThat(code).contains(String.format(CONSTRUCTOR, "Foo", "java.lang.String"));
    assertThat(code).contains(String.format(CLASS_DECLARATION, "Foo"));
    assertThat(code).contains(FOO_WRAPPED_METHOD);
  }

  public static class FooSubject extends Subject<FooSubject, String> {
    public static final SubjectFactory<FooSubject, String> FOO =
        new SubjectFactory<FooSubject, String>() {
          @Override
          public FooSubject getSubject(FailureStrategy fs, String target) {
            return new FooSubject(fs, target);
          }
        };

    public FooSubject(FailureStrategy failureStrategy, String subject) {
      super(failureStrategy, subject);
    }

    public void endsWith(String suffix) {
      if (getSubject().endsWith(suffix)) {
        fail("matches", getSubject(), suffix);
      }
    }
  }
}
