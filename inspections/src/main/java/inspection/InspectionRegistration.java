package inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
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
        // TODO: Locate diligent.json *within project*

//        File f = new File(".");
//        File[] matchingFiles = f.listFiles(new FilenameFilter() {
//            public boolean accept(File dir, String name) {
//                return name.startsWith("diligent") && name.endsWith("json");
//            }
//        });

        // Use it to turn on/off inspections
        Map<String, Class> config = new HashMap<>();
//        config.put("==-string", );
        config.put("inheritance", UsingInheritanceInspection.class);
        config.put("interfaces", UsingInterfacesInspection.class);
//        config.put("streams", );
//        config.put("for-loops", );
//        config.put("while-loops", );
        config.put("camelcase", CamelCaseInspection.class);
        config.put("screaming-snake-case", ScreamingSnakeCaseInspection.class);
        config.put("redundant-else", IfReturnElseInspection.class);
        config.put("fields-first", FieldsFirstInspection.class);
        config.put("single-char-name", SingleCharNameInspection.class);
        config.put("method-length", MethodLengthInspection.class);
        config.put("clone", CaseCloneDetectionInspection.class);


        List<Class> inspections = new ArrayList<>();
        try {
            Object obj = new JSONParser().parse(new FileReader("diligent.json"));
            JSONObject jo = (JSONObject) obj;
            JSONArray highInspections = (JSONArray) jo.get("high");
            JSONArray mediumInspections = (JSONArray) jo.get("medium");
            JSONArray lowInspections = (JSONArray) jo.get("low");

            for (Object inspection : highInspections) {
                String highInspection = (String) inspection;
                inspections.add(config.get(highInspection));
            }

            for (Object inspection : mediumInspections) {
                String mediumInspection = (String) inspection;
                inspections.add(config.get(mediumInspection));
            }

            for (Object inspection : lowInspections) {
                String lowInspection = (String) inspection;
                inspections.add(config.get(lowInspection));
            }

        } catch (IOException | ParseException e) {
            throw new RuntimeException("No diligent.json file found in '" + System.getProperty("user.dir") +"'.");
        }


        return inspections.toArray(new Class[0]);
    }
}
