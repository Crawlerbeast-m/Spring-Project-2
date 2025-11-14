pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/Crawlerbeast-m/Spring-Project-2.git'
        GIT_CREDENTIALS = 'Github'          // Jenkins GitHub credentials ID
        DOCKER_REPO = 'manish439/spring-project'  // or ECR repo URI
        DOCKER_CREDENTIALS = 'Docker'   // or ECR credentials ID
        KUBECONFIG_CREDENTIALS = 'kubeconfig-live'  // Jenkins kubeconfig secret file
        AWS_REGION = 'us-east-1'
        AWS_CREDS = 'aws-creds'      
    }

    triggers {
        githubPush()  // Automatically triggered via GitHub webhook
    }

    stages {

        stage('Checkout Source') {
            steps {
                git branch: 'master', credentialsId: "${GIT_CREDENTIALS}", url: "${GIT_REPO}"
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    docker.withRegistry('', "${DOCKER_CREDENTIALS}") {
                        def app = docker.build("${DOCKER_REPO}:${BUILD_NUMBER}")
                        app.push()
                        app.push('latest')
                    }
                }
            }
        }

        // stage('Tag Git Commit') {
        //     steps {
        //         sh """
        //             git config user.email "jenkins@ci-server.local"
        //             git config user.name "Jenkins CI"
        //             git tag -a v${BUILD_NUMBER} -m "Build ${BUILD_NUMBER} deployed to EKS"
        //             git push origin v${BUILD_NUMBER}
        //         """
        //     }
        // }

         stage('Create EKS Cluster (First Time Only)') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: "${AWS_CREDS}"
                ]]) {

                    sh """
                        echo "Creating EKS cluster if it does not exist..."

                        if ! eksctl get cluster --name jt-cluster --region ${AWS_REGION} >/dev/null 2>&1; then
                            echo "Cluster does not exist. Creating now..."
                            eksctl create cluster \
                                --name jt-cluster \
                                --version 1.28 \
                                --nodes=1 \
                                --node-type=t2.small \
                                --region ${AWS_REGION}
                        else
                            echo "Cluster already exists. Skipping creation."
                        fi

                        echo "Updating kubeconfig for jt-cluster..."
                        aws eks --region ${AWS_REGION} update-kubeconfig --name jt-cluster
                    """
                }
            }
        }

        stage('Deploy to Amazon EKS') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: "${AWS_CREDS}"
                ]]) {

                    sh """
                        echo "Deploying to EKS..."

                        kubectl set image deployment/spring-project \
                            spring-project=${DOCKER_REPO}:${BUILD_NUMBER} \
                            -n production

                        kubectl rollout status deployment/spring-project -n production
                        kubectl get pods -n production
                    """
                }
            }
        }
    }

    post {
        success {
            echo "CI/CD to Amazon EKS successful!"
        }
        failure {
            echo "Build or deployment failed!"
        }
    }
}




------------ without eks cluster creation stage -------------


pipeline {
    agent any

    environment {
        GIT_REPO                = 'https://github.com/Crawlerbeast-m/Spring-Project-2.git'
        GIT_CREDENTIALS         = 'Github'
        DOCKER_REPO             = 'manish439/spring-project'
        DOCKER_CREDENTIALS      = 'Docker'
        KUBECONFIG_CREDENTIALS  = 'kubeconfig-live'
        AWS_CREDS               = 'aws-creds'
        AWS_REGION              = 'us-east-1'
    }

    triggers {
        githubPush()
    }

    stages {

        stage('Checkout Source') {
            steps {
                git branch: 'master', credentialsId: "${GIT_CREDENTIALS}", url: "${GIT_REPO}"
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    docker.withRegistry('', "${DOCKER_CREDENTIALS}") {
                        def app = docker.build("${DOCKER_REPO}:${BUILD_NUMBER}")
                        app.push()
                        app.push('latest')
                    }
                }
            }
        }

        stage('Deploy to Amazon EKS') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: "${AWS_CREDS}"]]) {
                    sh '''
                        aws eks update-kubeconfig --name java-spring-cluster --region us-east-1
                        kubectl get ns
                        kubectl apply -f k8s.yaml
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "CI/CD Pipeline to EKS Completed Successfully!"
        }
        failure {
            echo "Pipeline failed. Check logs."
        }
    }
}
