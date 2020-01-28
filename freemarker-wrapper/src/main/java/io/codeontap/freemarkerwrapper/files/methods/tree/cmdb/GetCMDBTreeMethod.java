package io.codeontap.freemarkerwrapper.files.methods.tree.cmdb;

import freemarker.core.Environment;
import freemarker.template.*;
import io.codeontap.freemarkerwrapper.files.adapters.JsonStringAdapter;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;
import io.codeontap.freemarkerwrapper.files.meta.cmdb.CMDBMeta;
import io.codeontap.freemarkerwrapper.files.processors.cmdb.CMDBProcessor;

import javax.json.JsonObject;
import java.util.*;

public class GetCMDBTreeMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Wrong arguments");
        }

        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();
        Object startingPathObj = args.get(0);
        String startingPath = null;
        if (startingPathObj instanceof SimpleScalar){
            startingPath = startingPathObj.toString();
        }else if (startingPathObj instanceof JsonStringAdapter){
            startingPath = ((JsonStringAdapter) startingPathObj).getAsString();
        }
        TemplateHashModelEx options = (TemplateHashModelEx)args.get(1);
        TemplateModelIterator iterator = options.keys().iterator();
        TemplateSequenceModel regexSequence = null;
        SimpleScalar regexScalar = null;
        boolean ignoreDotDirectories = Boolean.TRUE;
        boolean ignoreDotFiles = Boolean.TRUE;
        boolean includeCMDBInformation = Boolean.FALSE;
        boolean useCMDBPrefix = Boolean.FALSE;
        boolean addStartingWildcard = Boolean.TRUE;
        boolean addEndingWildcard = Boolean.TRUE;
        while (iterator.hasNext()){
            TemplateModel key = iterator.next();
            if ("Regex".equalsIgnoreCase(key.toString())){
                Object regex = options.get("Regex");
                if(regex instanceof TemplateSequenceModel)
                    regexSequence = (TemplateSequenceModel)regex;
                else if(regex instanceof SimpleScalar)
                    regexScalar = (SimpleScalar)regex;
            } else if ("IgnoreDotDirectories".equalsIgnoreCase(key.toString())){
                ignoreDotDirectories = ((TemplateBooleanModel) options.get("IgnoreDotDirectories")).getAsBoolean();
            } else if ("IgnoreDotFiles".equalsIgnoreCase(key.toString())){
                ignoreDotFiles = ((TemplateBooleanModel) options.get("IgnoreDotFiles")).getAsBoolean();
            } else if ("IncludeCMDBInformation".equalsIgnoreCase(key.toString())){
                includeCMDBInformation = ((TemplateBooleanModel) options.get("IncludeCMDBInformation")).getAsBoolean();
            } else if ("UseCMDBPrefix".equalsIgnoreCase(key.toString())){
                useCMDBPrefix = ((TemplateBooleanModel) options.get("UseCMDBPrefix")).getAsBoolean();
            } else if ("AddStartingWildcard".equalsIgnoreCase(key.toString())){
                addStartingWildcard = ((TemplateBooleanModel) options.get("AddStartingWildcard")).getAsBoolean();
            } else if ("AddEndingWildcard".equalsIgnoreCase(key.toString())){
                addEndingWildcard = ((TemplateBooleanModel) options.get("AddEndingWildcard")).getAsBoolean();
            }
        }
        List<String> regexList = new ArrayList<>();
        if(regexSequence == null || regexSequence.size() == 0){
            if(regexScalar == null) {
                regexList.add("^.*$");
            } else {
                regexList.add(regexScalar.getAsString());
            }
        } else {
            for (int i=0; i < regexSequence.size();i++){
                regexList.add(regexSequence.get(i).toString());
            }
        }

        CMDBProcessor cmdbProcessor = new CMDBProcessor();
        Set<JsonObject> result = null;
        try {
            CMDBMeta cmdbMeta = new CMDBMeta();
            cmdbMeta.setLookupDirs(lookupDirs);
            cmdbMeta.setCMDBs(cmdbPathMapping);
            cmdbMeta.setCMDBNamesList(CMDBNames);
            cmdbMeta.setBaseCMDB(baseCMDB);
            cmdbMeta.setStartingPath(startingPath);
            cmdbMeta.setRegexList(regexList);
            cmdbMeta.setIgnoreDotDirectories(ignoreDotDirectories);
            cmdbMeta.setIgnoreDotFiles(ignoreDotFiles);
            cmdbMeta.setIncludeInformation(includeCMDBInformation);
            cmdbMeta.setUseCMDBPrefix(useCMDBPrefix);
            cmdbMeta.setAddStartingWildcard(addStartingWildcard);
            cmdbMeta.setAddEndingWildcard(addEndingWildcard);
            result = cmdbProcessor.getLayerTree(cmdbMeta);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }

        return new SimpleSequence(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}