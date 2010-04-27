/*
 * Copyright (c) 2010 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bigfatgun.fixjures;

import static com.bigfatgun.fixjures.Fixjure.of;
import static com.bigfatgun.fixjures.yaml.YamlSource.newYamlString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class FixtureIntegrationTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateFixjureOfConcreteLocalClass() {
        class Foo {}
        of(Foo.class);
    }

    @Test
    public void concretePublicStaticInnerClassFromYamlWithSimplePrimitives1() {
        ConcreteBeanWithPrimitives obj = of(ConcreteBeanWithPrimitives.class).from(newYamlString("string: string value\n" +
                "bool: true\n" +
                "b: 1\n" +
                "s: 2\n" +
                "i: 3\n" +
                "l: 4\n" +
                "f: 5\n" +
                "d: 6")).create();

        assertNotNull(obj);
        assertEquals("string value", obj.getString());
        assertTrue(obj.isBool());
        assertEquals((byte) 1, obj.getB());
        assertEquals((short) 2, obj.getS());
        assertEquals(3, obj.getI());
        assertEquals(4L, obj.getL());
        assertEquals(5f, obj.getF(), 0.0f);
        assertEquals(6d, obj.getD(), 0.0d);
    }

    @Test(expected = FixtureException.class)
    public void concretePublicStaticInnerClassFromYamlWithSomeUnmappableData() {
        of(ConcreteBeanWithPrimitives.class).from(newYamlString("foobar: totally fubar\n")).create();
    }

    @Test
    public void concretePublicStaticInnerClassFromYamlWithSomeUnmappableDataWithSkipUnmappable() {
        ConcreteBeanWithPrimitives obj = of(ConcreteBeanWithPrimitives.class)
                .from(newYamlString("foobar: totally fubar\n"))
                .withOptions(Fixjure.Option.SKIP_UNMAPPABLE)
                .create();
        assertNotNull(obj);
        assertNull(obj.getString());
    }

    @Test
    public void concretePublicStaticInnerClassFromYamlWithSimplePrimitives2() {
        ConcreteBeanWithPrimitives obj = of(ConcreteBeanWithPrimitives.class).from(newYamlString("s:null\n" +
                "bool: false\n" +
                "b: -1\n" +
                "s: -2\n" +
                "i: -3\n" +
                "l: -4\n" +
                "f: -5\n" +
                "d: -6")).create();
        assertNull(obj.getString());
        assertFalse(obj.isBool());
        assertEquals((byte) -1, obj.getB());
        assertEquals((short) -2, obj.getS());
        assertEquals(-3, obj.getI());
        assertEquals(-4L, obj.getL());
        assertEquals(-5f, obj.getF(), -0.0f);
        assertEquals(-6d, obj.getD(), 0.0d);
    }

    public static class ConcreteBeanWithPrimitives {
        private String string;
        private boolean bool;
        private int i;
        private long l;
        private byte b;
        private short s;
        private float f;
        private double d;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public long getL() {
            return l;
        }

        public void setL(long l) {
            this.l = l;
        }

        public byte getB() {
            return b;
        }

        public void setB(byte b) {
            this.b = b;
        }

        public short getS() {
            return s;
        }

        public void setS(short s) {
            this.s = s;
        }

        public float getF() {
            return f;
        }

        public void setF(float f) {
            this.f = f;
        }

        public double getD() {
            return d;
        }

        public void setD(double d) {
            this.d = d;
        }
    }
}
