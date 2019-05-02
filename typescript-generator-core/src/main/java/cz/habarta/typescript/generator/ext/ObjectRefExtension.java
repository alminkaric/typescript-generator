
package cz.habarta.typescript.generator.ext;

import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.compiler.*;
import cz.habarta.typescript.generator.emitter.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;


public class ObjectRefExtension extends Extension {

    private String PERS_OBJ_INTERFACE = "PersistentObjectInterface";
    private String EXCLUDE_TYPES = "excludeTypes";
    private String persistentObjectInterface = "PersistentObject";
    private List<String> excludeTypes = new ArrayList<String>();

    @Override
    public EmitterExtensionFeatures getFeatures() {
        return new EmitterExtensionFeatures();
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) throws RuntimeException {
        if (configuration.containsKey(PERS_OBJ_INTERFACE))
            this.persistentObjectInterface = configuration.get(PERS_OBJ_INTERFACE);

        if (configuration.containsKey(EXCLUDE_TYPES)) {
            excludeTypes = Arrays.asList(configuration.get(EXCLUDE_TYPES).split(",\\s*"));
        }
    }

    @Override
    public List<TransformerDefinition> getTransformers() {
        return Arrays.asList(new TransformerDefinition(ModelCompiler.TransformationPhase.BeforeSymbolResolution, new ModelTransformer() {
            @Override
            public TsModel transformModel(SymbolTable symbolTable, TsModel model) {
                List<TsBeanModel> beans = model.getBeans();

                for (TsBeanModel bean : beans) {
                    for ( TsPropertyModel prop : bean.getProperties() ) {
                        if ( propIsReferenceType(prop, bean) )
                            prop.isRefObj = true;
                    }
                }

                return model;
            }
        }));
    }

    private boolean propIsReferenceType(TsPropertyModel prop, TsBeanModel bean) {

        TsType tsType = prop.getTsType();
        Class<?> actualClass = findClassOfProp(prop, bean);

        if (tsType instanceof  TsType.OptionalType)
            tsType = ((TsType.OptionalType) tsType).type;

        if ( tsType instanceof TsType.BasicArrayType)
            actualClass = findClassOfList(prop, bean);

        if (actualClass == null)
            return false;

        if (isExcludedType(actualClass))
            return false;

        if (classImplementsPersistentObjectInterface(actualClass))
            return  true;

        return false;
    }

    private Class<?> findClassOfProp(TsPropertyModel prop, TsBeanModel bean) {
        Field declaredField = null;

        try {
            declaredField = bean.getOrigin().getDeclaredField(prop.getName());
            return declaredField.getType();
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private Class<?> findClassOfList(TsPropertyModel prop, TsBeanModel bean) {
        Field declaredField = null;

        try {
            declaredField = bean.getOrigin().getDeclaredField(prop.getName());
            ParameterizedType pt = (ParameterizedType) declaredField.getGenericType();
            Class<?> actualClass = (Class<?>) pt.getActualTypeArguments()[0];
            return actualClass;
        } catch (NoSuchFieldException | ClassCastException e) {
            return null;
        }
    }

    private boolean classImplementsPersistentObjectInterface(Class<?> klass) {
        for ( Class<?> classInterface : getAllExtendedOrImplementedTypesRecursively(klass)) {
            if (classInterface.getSimpleName().equals(persistentObjectInterface))
                return true;
        }

        return false;
    }

    private boolean isExcludedType(Class<?> klass) {
        for (String excludedType : excludeTypes) {
            if (excludedType.equals(klass.getSimpleName()))
                return true;
        }

        return false;
    }

    public static Set<Class<?>> getAllExtendedOrImplementedTypesRecursively(Class<?> clazz) {
        List<Class<?>> res = new ArrayList<>();

        do {
            res.add(clazz);

            // First, add all the interfaces implemented by this class
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                res.addAll(Arrays.asList(interfaces));

                for (Class<?> interfaze : interfaces) {
                    res.addAll(getAllExtendedOrImplementedTypesRecursively(interfaze));
                }
            }

            // Add the super class
            Class<?> superClass = clazz.getSuperclass();

            // Interfaces does not have java,lang.Object as superclass, they have null, so break the cycle and return
            if (superClass == null) {
                break;
            }

            // Now inspect the superclass
            clazz = superClass;
        } while (!"java.lang.Object".equals(clazz.getCanonicalName()));

        return new HashSet<Class<?>>(res);
    }

}
