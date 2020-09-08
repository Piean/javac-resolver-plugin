package com.mogu.module.javac;

import com.thoughtworks.qdox.model.JavaClass;
import com.yunhu.common.javadoc.JavaSourceReader;
import com.yunhu.common.maven.PomCoords;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dishuang.yang@yunhuyj.com">mogu</a>
 * @since 2020/9/4
 */
@SuppressWarnings("unused")
@Mojo(name = "javac-resolver-plugin", defaultPhase = LifecyclePhase.DEPLOY)
public class JavaSourceResolver extends AbstractMojo {

    @Parameter(property = "project.build.sourceDirectory", required = true, readonly = true)
    private String projectDir;
    @Parameter(property = "project.groupId", required = true, readonly = true)
    private String groupId;
    @Parameter(property = "project.artifactId", required = true, readonly = true)
    private String artifactId;
    @Parameter(property = "project.version", required = true, readonly = true)
    private String version;



    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        PomCoords pom = new PomCoords(groupId, artifactId, version);
        pom.setClassifier(PomCoords.CLASSIFIER_SOURCE);
        String coords = pom.buildCoords();
        System.out.println("Read java source for " + coords + " in dir:" + projectDir);
        if (StringUtils.isAnyBlank(projectDir, groupId, artifactId, version)) {
            throw new MojoFailureException("Maven project information is incomplete:" + coords);
        }

        try {
            Map<String, JavaClass> classMap = JavaSourceReader.readSourceFiles(Collections.singletonList(projectDir));

            Map<String, Object> params = new HashMap<>();
            params.put(coords, classMap.keySet());
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity("http://localhost:8080/pharmacy/merchant/javadoc", params, String.class);
        } catch (Exception e) {
            throw new MojoExecutionException("Parse Java Source Occurs An exception", e);
        }
    }
}
