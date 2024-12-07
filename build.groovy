pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'asisgolu95/demo-app'
        REMOTE_USER = "demo-user"            // SSH user for remote server
        REMOTE_HOST = "3.110.207.17"       // Remote server IP or hostname
        SSH_CREDENTIALS = 'demo-user'  // Jenkins SSH credential ID
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
                    sshagent([SSH_CREDENTIALS]) {
                        // Copy docker-compose.yml or other required files to the remote server
                        sh """
                        scp docker-compose.yml ${REMOTE_USER}@${REMOTE_HOST}:${DOCKER_COMPOSE_DIR}/
                        """
                    }
                }
            }
        }

        stage('Deploy on Remote Server') {
            steps {
                script {
                    sshagent([SSH_CREDENTIALS]) {
                        // Run docker-compose on the remote server
                        sh """
                        ssh ${REMOTE_USER}@${REMOTE_HOST} 'cd ${DOCKER_COMPOSE_DIR} && docker-compose down && docker-compose up -d'
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            // Clean workspace after pipeline execution
            cleanWs()
        }
    }
}
