package cz.habarta.typescript.generator;

import cz.habarta.typescript.generator.ext.ObjectRefExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ObjectRefPropertiesTest {

    @Test
    public void test() {
        final Settings settings = TestUtils.settings();
        settings.sortDeclarations = true;
        final ObjectRefExtension objRefExt = new ObjectRefExtension();
        Map<String, String> configuration = new HashMap<>();
        configuration.put("PersistentObjectInterface", PersistentObject.class.getName());
        objRefExt.setConfiguration(configuration);
        settings.extensions.add(objRefExt);
        final String output = new TypeScriptGenerator(settings).generateTypeScript(Input.from(Person.class));
        final String expected =
                "export interface ObjectRef<T> {\n" +
                "    uid: string;\n" +
                "}\n" +
                "\n" +
                "interface Client extends PersistentObject {\n" +
                "    person: ObjectRef<Person>;\n" +
                "    uid: string;\n" +
                "}\n" +
                "\n" +
                "interface Group extends PersistentObject {\n" +
                "    person: ObjectRef<Person>;\n" +
                "    uid: string;\n" +
                "}\n" +
                "\n" +
                "interface PersistentObject {\n" +
                "}\n" +
                "\n" +
                "interface Person extends PersistentObject {\n" +
                "    client: ObjectRef<Client>;\n" +
                "    groups: ObjectRef<Group>[];\n" +
                "    tags: string[];\n" +
                "    tagsAsList: string[];\n" +
                "    uid: string;\n" +
                "}";
        Assert.assertEquals(expected.replace('\'', '"'), output.trim());
    }

    private  interface PersistentObject {
    }

    private static abstract class Person implements PersistentObject  {
        public String uid;
        public Client client;
        public List<Group> groups;
        public List<String> tags;

        public List<String> getTagsAsList() {
            return new ArrayList<>();
        }
    }

    private static abstract class Client implements PersistentObject {
        public String uid;
        public Person person;
    }

    private static abstract class Group implements PersistentObject {
        public String uid;
        public Person person;
    }

}
