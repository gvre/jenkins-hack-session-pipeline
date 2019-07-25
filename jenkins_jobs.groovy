folder('yorgos') {
    description("Playground for yorgos' pipelines")
}

multibranchPipelineJob('yorgos/sample-pipeline') {
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
        }
    }
    branchSources {
        git {
            id('origin')
            remote('https://github.com/gvre/jenkins-hack-session-pipeline')
            excludes('excluded')
        }
    }
    factory {
        workflowBranchProjectFactory {
            scriptPath('Jenkinsfile')
        }
    }
}