pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        withMaven(maven: '3.5.3', publisherStrategy: 'IMPLICIT') {
          sh 'mvn clean install'
        }
        
      }
    }
    stage('Notify') {
      steps {
        slackSend(message: 'Message from Jenkins Pipeline', color: 'good')
      }
    }
  }
}