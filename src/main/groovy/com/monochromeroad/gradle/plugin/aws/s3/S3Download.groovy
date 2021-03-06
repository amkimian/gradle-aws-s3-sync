package com.monochromeroad.gradle.plugin.aws.s3

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.PathValidation
import org.jets3t.apps.synchronize.Synchronize
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.security.AWSCredentials
import org.jets3t.service.Jets3tProperties
import org.jets3t.service.Constants

class S3Download extends DefaultTask {

    def accessKey

    def secretKey

    def quiet

    def noProgress

    def force

    def keepFiles

    def noDelete

    def gzipEnabled

    def encryptionEnabled

    def moveEnabled

    def batchMode

    def configFile

    ACL acl = ACL.Private

    ReportLevel reportLevel = ReportLevel.All

    private File destDir

    private String source

    void from(bucketLocation) {
        source = bucketLocation 
    }

    void into(destinationPath) {
        destDir = project.file(destinationPath) 
    }

    @TaskAction
    def sync() {
        def awsCredentials = new AWSCredentials(accessKey, secretKey)
        def s3Service = new RestS3Service(awsCredentials)

        Jets3tProperties properties = loadProperties()

        boolean doAction = true;
        boolean isQuiet = quiet == true;
        boolean isNoProgress = noProgress == true;
        boolean isForce = force == true;
        boolean isKeepFiles = keepFiles == true;
        boolean isNoDelete = noDelete == true;
        boolean isGzipEnabled = gzipEnabled == true;
        boolean isEncryptionEnabled = encryptionEnabled == true;
        boolean isMoveEnabled = moveEnabled == true;
        boolean isBatchMode = batchMode == true;
        String aclString = acl.toString()

        Synchronize client = new Synchronize(
                s3Service, doAction, isQuiet, isNoProgress, isForce, isKeepFiles, isNoDelete,
                isMoveEnabled, isBatchMode, isGzipEnabled, isEncryptionEnabled,
                reportLevel.level, properties);

        File[] dests = new File[1];
        dests[0] = destDir; 
        client.run(source, dests,
                    "DOWN",
                    properties.getStringProperty("password", null), aclString,
                    "S3");

    }

    Jets3tProperties loadProperties() {
        Jets3tProperties myProperties =
            Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME);

        // Read the Synchronize properties file from the classpath
        File synchronizeProperties = project.file(configFile, PathValidation.FILE)
        if (synchronizeProperties.canRead()) {
            synchronizeProperties.withInputStream {
                myProperties.loadAndReplaceProperties(it, configFile + " in the user config")
            }
        } else {
            throw new IllegalStateException("the config file cannot be read : " + synchronizeProperties.absolutePath)
        }
        return myProperties
    }
}
