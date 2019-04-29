
package cz.habarta.typescript.generator;

import cz.habarta.typescript.generator.ext.ObjectRefExtension;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;


public class ObjectRefPropertiesTest {

    @Test
    public void test() {
        final Settings settings = TestUtils.settings();
        settings.sortDeclarations = true;
        final ObjectRefExtension objRefExt = new ObjectRefExtension(true);
        settings.extensions.add(objRefExt);
        final String output = new TypeScriptGenerator(settings).generateTypeScript(Input.from(Person.class));
        final String expected =
                "interface Client {\n" +
                "    person: ObjectRef<Person>;\n" +
                "    uid: string;\n" +
                "}\n" +
                "\n" +
                "interface Group {\n" +
                "    person: ObjectRef<Person>;\n" +
                "    uid: string;\n" +
                "}\n" +
                "\n" +
                "interface Person {\n" +
                "    client: ObjectRef<Client>;\n" +
                "    groups: ObjectRef<Group>[];\n" +
                "    tags: string[];\n" +
                "    uid: string;\n" +
                "}";
        Assert.assertEquals(expected.replace('\'', '"'), output.trim());
    }

    private static abstract class Person {
        public String uid;
        public Client client;
        public List<Group> groups;
        public List<String> tags;
    }

    private static abstract class Client {
        public String uid;
        public Person person;
    }

    private static abstract class Group {
        public String uid;
        public Person person;
    }

}
