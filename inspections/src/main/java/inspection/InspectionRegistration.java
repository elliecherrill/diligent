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
                CaseCloneDetectionInspection.class,
                FieldsFirstInspection.class,
                IfReturnElseInspection.class,
                MethodLengthInspection.class,
                ScreamingSnakeCaseInspection.class,
                SingleCharNameInspection.class,
                //TODO: turn these on once they are finished
//                UsingForLoopsInspection.class,
//                UsingInheritanceInspection.class,
//                UsingInterfacesInspection.class,
//                UsingStreamsInspection.class,
//                UsingWhileLoopsInspection.class
        };

    }
}
