pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'asisgolu95/demo-app'
        REMOTE_USER = "demo-user"            // SSH user for remote server
        SERVER_PRIVATE_IP = "13.232.205.26"       // Remote server IP or hostname
        CREDENTIALSID = 'demo-user'  // Jenkins SSH credential ID
        DOCKER_COMPOSE_DIR = "/home/demo-user/" // Directory on remote server
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/master']],
                        userRemoteConfigs: [[url: 'https://github.com/Ashish2seatech/testing.git']]
                    ]
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh 'sudo docker build -t ${DOCKER_IMAGE} .'
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker_hub_new', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh 'sudo docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}'
                        sh 'sudo docker push ${DOCKER_IMAGE}'
                    }
                }
            }
        }

        stage('Copy Files to Remote Server') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: "$CREDENTIALSID", passwordVariable: 'password', usernameVariable: 'username')]) {
                        // Copy docker-compose.yml or other required files to the remote server
                        sh """
                        sshpass -p $password scp docker-compose.yml $username@$SERVER_PRIVATE_IP:${DOCKER_COMPOSE_DIR}/
                        """
                    }
                }
            }
        }

        stage('Deploy on Remote Server') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: "$CREDENTIALSID", passwordVariable: 'password', usernameVariable: 'username')]) {
                        // Run docker-compose on the remote server
                        sh """
                        sshpass -p $password ssh -t -q -o StrictHostKeyChecking=no $username@$SERVER_PRIVATE_IP<<'EOSSH' 'cd ${DOCKER_COMPOSE_DIR} && docker-compose down && docker-compose up -d'
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            emailext(
                to: 'asis.golu@gmail.com,ashishsmart445@gmail.com',
                subject: "Jenkins Pipeline Success: ${currentBuild.fullDisplayName}",
                body: "The Jenkins pipeline has completed successfully. Build details:\n\n" +
                      "Project: ${env.JOB_NAME}\n" +
                      "Build Number: ${env.BUILD_NUMBER}\n" +
                      "Status: SUCCESS"
            )
        }
        failure {
            emailext(
                to: 'asis.golu@gmail.com,ashishsmart445@gmail.com',
                subject: "Jenkins Pipeline Failure: ${currentBuild.fullDisplayName}",
                body: "The Jenkins pipeline has failed. Please review the logs for details:\n\n" +
                      "Project: ${env.JOB_NAME}\n" +
                      "Build Number: ${env.BUILD_NUMBER}\n" +
                      "Status: FAILURE"
            )
        }
        always {
            cleanWs()
        }
    }
}
