package com.bihe0832

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

class GradleDependenciesCheckPlugin implements Plugin<Project> {

    //验证插件功能的命令
    private static final String TASK_TEST_PLUGIN = "zixietest"

    //检测依赖是否冲突的命令
    private static final String TASK_LIST_DEPENDENCIE = "checkGradleDependencies"

    //检测依赖是否冲突的命令
    private static final String PROTERTY_NAME = "GradleDependenciesCheckConfig"

    @Override
    void apply(Project project) {
        project.extensions.create(PROTERTY_NAME, GradleDependenciesCheckPluginExtension)
        def extension = project.extensions.findByName(PROTERTY_NAME) as GradleDependenciesCheckPluginExtension
        CheckResult.sGroupsHasBeenShowed.clear()
        CheckResult.sGroupList.clear()

        project.task(TASK_TEST_PLUGIN) << {
            println "hello, world! , GradleDependenciesCheckPlugin is ok !!!"
        }

        project.task(TASK_LIST_DEPENDENCIE) << {
            println(TASK_LIST_DEPENDENCIE + " start ...")

            project.configurations.each { org.gradle.api.artifacts.Configuration conf ->
                if(canBeResolved(conf) && needResolved(conf)) {
                    conf.incoming.resolutionResult.root.dependencies.each { DependencyResult dr ->
                        if (dr instanceof ResolvedDependencyResult) {
//                            dr.selected.moduleVersion:com.android.support:support-v4:27.0.1
//                            dr.selected.moduleVersion.group:com.android.support
//                            dr.selected.moduleVersion.name:support-v4
//                            dr.selected.moduleVersion.version:27.0.1
//                            c:com.android.support:support-v4
//                            dr.selected.moduleVersion.module.group:com.android.support
//                            dr.selected.moduleVersion.module.name:support-v4
//                            dr.selected.moduleVersion:com.android.support:support-v4:27.0.1
                            String groupAndId = dr.selected.moduleVersion.module
                            String version = dr.selected.moduleVersion.version
                            String source = project.name + ":"+ groupAndId + ":"+ version
                            if(null != version && !version.equalsIgnoreCase("unspecified") && version.length() > 0){
                                checkDependencies(extension.showResultType ,source, dr)
                            }
                        }else{
//                            println("Could not resolve $dr.requested.displayName")
                        }
                    }
                }
            }
            println(TASK_LIST_DEPENDENCIE + " finished Successfully!")
        }
    }


    static boolean needResolved(Configuration conf) {
        if(conf.name == "lintClassPath"){
            return false
        }else {
            return true
        }
    }

    static boolean canBeResolved(Configuration conf) {
        try {
            // this method doesn't exist before Gradle 3
            return conf.isCanBeResolved();
        } catch (Exception e) {
            // assume everything can be resolved for Gradle < 3
            return true;
        }
    }

    static void checkDependencies(int type, String source, DependencyResult result) {
        if (result instanceof ResolvedDependencyResult) {
            ResolvedDependencyResult dr = result
            String groupAndId = dr.selected.moduleVersion.module
            String version = dr.selected.moduleVersion.version
            if(null != version && !version.equalsIgnoreCase("unspecified") && version.length() > 0){
                CheckResult.doCheck(type,groupAndId,version,source)
            }
            dr.selected.dependencies.each { DependencyResult subDep ->
                checkDependencies(type ,source, subDep)
            }
        } else {
//            println("Could not resolve $result.requested.displayName")
        }

    }
}


