package com.osmagic.pipeline.sharding.grammar

import groovy.text.GStringTemplateEngine

class TemplateVerify {

    static void main(String[] args) {

        def ctx = '''
        start ---------- 
        <% env.each { item ->  %>
        name: ${item.name}
        <% }  %>
        finish ---------- 
        '''

        def map = [env: [
                ["name": "STQF"],
                ["name": "Smith"],
                ["name": "Jhon"]
        ]]

        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(ctx)
        def make = template.make(map)
        println(make)

        def shell = GroovyShell.newInstance()
        shell.evaluate("println('GroovyShell':1+5)")
    }

}
