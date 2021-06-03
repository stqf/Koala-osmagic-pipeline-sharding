def call(String name) {
    /*执行devops/Package/main.sh命令,生成整包*/
    def packWs = "${env.WORKSPACE}/devops/Package"
    println("[TRACE] Pack start  ... name is $name, workspace is $packWs bashItem: 'main.sh'")
    sh """
        cd $packWs && bash main.sh
    """
    println("[DEBUG] Pack finish ... name is $name, workspace is $packWs bashItem: 'main.sh'")
}