package inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;


public final class InspectionRegistration implements ApplicationComponent, InspectionToolProvider {

    public InspectionRegistration() {
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "InspectionRegistration";
    }

    @Override
    @NotNull
    public Class<? extends LocalInspectionTool>[] getInspectionClasses() {
        return new Class[]{
                CamelCaseInspection.class,
                FieldsFirstInspection.class,
                IfReturnElseInspection.class,
                CloneInspection.class,
                MethodLengthInspection.class,
                ScreamingSnakeCaseInspection.class,
                ShorthandInspection.class,
                SimplifyIfInspection.class,
                SingleCharNameInspection.class,
                StringComparisonInspection.class,
                StringConcatInspection.class,
                ThisInspection.class,
                UsingForLoopsInspection.class,
                UsingInheritanceInspection.class,
                UsingInterfacesInspection.class,
                UsingStreamsInspection.class,
                UsingWhileLoopsInspection.class,
        };

    }
}
