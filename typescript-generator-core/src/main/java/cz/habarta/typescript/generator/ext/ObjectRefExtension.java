
package cz.habarta.typescript.generator.ext;

import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.compiler.*;
import cz.habarta.typescript.generator.emitter.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;


public class ObjectRefExtension extends Extension {

    public static final String USE_OBJECT_REF = "useObjectRef";

    private boolean useObjectRef = false;

    public ObjectRefExtension() {
    }

    public ObjectRefExtension(boolean useObjectRef) {
        this.useObjectRef = useObjectRef;
    }

    @Override
    public EmitterExtensionFeatures getFeatures() {
        return new EmitterExtensionFeatures();
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
        if (prop.getTsType() instanceof TsType.ReferenceType)
            return true;

        if ( prop.getTsType() instanceof TsType.BasicArrayType )  {
            Class<?> propClass = findClassOfProp(prop, bean);
            return  hasFieldUid(propClass);
        }

        return false;
    }

    private Class<?> findClassOfProp(TsPropertyModel prop, TsBeanModel bean) {
        Field declaredField = null;
        try {
            declaredField = bean.getOrigin().getDeclaredField(prop.getName());
            ParameterizedType genericType = (ParameterizedType) declaredField.getGenericType();
            Class<?> actualClass = (Class<?>) genericType.getActualTypeArguments()[0];
            return  actualClass;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private boolean hasFieldUid(Class<?> klass) {
        try {
            klass.getDeclaredField("uid");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
