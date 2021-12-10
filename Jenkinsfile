#!groovy

@Library('pipelib@github-creds')
import org.veupathdb.lib.Builder

node('centos8') {
  sh "env"

  def builder = new Builder(this)

  checkout scm

  // See the docs at https://github.com/VEuPathDB/pipelib/blob/main/src/org/veupathdb/lib/Builder.groovy#L41
  builder.buildContainers([
    [ name: 'demo-service' ],
  ])
}
