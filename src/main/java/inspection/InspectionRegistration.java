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
                IfReturnElseInspection.class,
                FieldsFirstInspection.class,
                CamelCaseInspection.class,
                SingleCharNameInspection.class,
//                MethodLengthInspection.class,
                UsingInheritanceInspection.class,
                UsingInterfacesInspection.class,
                CaseCloneDetectionInspection.class
        };
    }
}
