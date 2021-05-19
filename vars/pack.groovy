def call(String name) {
    def packWs = "${env.WORKSPACE}/devops/Package"
    println("Pack[$name] start  ... wsItem:$packWs bashItem:main.sh")
    sh """
        cd $packWs && bash main.sh
    """
    println("Pack[$name] finish ... wsItem:$packWs bashItem:main.sh")
}