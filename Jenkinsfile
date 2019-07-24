#!groovy

pipeline {
    agent none

    stages {
        stage('Checkout') {
            agent any

            steps {
                git branch: 'master',
                    credentialsId: 'github_ssh_key',
                    url: 'git@github.com:gvre/jenkins-hack-session-project.git'

                sh '''
                    rm -rf /tmp/jenkins-hack-session-project
                    cp -aT . /tmp/jenkins-hack-session-project
                '''
            }
        }

        stage('Build') {
            agent {
                dockerfile {
                    dir '/tmp/jenkins-hack-session-project'
                    filename 'Dockerfile.build'
                    args '-u root:root \
                          -w /var/www/jenkins-hack-session-project \
                          -v /tmp/jenkins-hack-session-project:/var/www/jenkins-hack-session-project:rw'
                }
            }

            steps {
                withCredentials([
                    sshUserPrivateKey(credentialsId: 'github_ssh_key', keyFileVariable: 'ssh_key_file', passphraseVariable: 'ssh_key_pass')
                ]) {
                    sh '''
                        eval "$(ssh-agent)"
                        echo "$ssh_key_pass" | ssh-add "$ssh_key_file"
                        cd /var/www/jenkins-hack-session-project
                        make staging
                        make lint test
                        make production
                        chmod -R 777 vendor
                    '''
                }
            }
        }

        stage('Deploy') {
            agent any

            steps {
                ansiblePlaybook(
                    credentialsId: 'server_ssh_key',
                    inventory: 'hosts',
                    playbook:  'deploy.yml',
                    extraVars: [
                        project: 'jenkins-hack-session-project',
                        basedir: '/tmp/jenkins-hack-session-project',
                        target:  'production'
                    ]
                )
            }
        }
    }

    post {
        success {
           slackSend baseUrl: 'https://hooks.slack.com/services/', \
                     channel: '#general',
                     color:   'good',                              \
                     message: 'Deployment to production completed. Build: ' + BUILD_ID, \
                     tokenCredentialId: 'slack'
        }

        failure {
           slackSend baseUrl: 'https://hooks.slack.com/services/', \
                     channel: '#general',
                     color:   'danger',                            \
                     message: 'Build and/or deployment to production failed. Build: ' + BUILD_ID, \
                     tokenCredentialId: 'slack'
        }
    }
}
