package inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
