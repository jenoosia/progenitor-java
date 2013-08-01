module.exports = function(grunt) {
    
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        
        /* Tasks */
        
        compass: {
            dev: {
                options: {
                    config: 'compass/config.rb',
                    basePath: 'compass/'
                }
            }
        },
        
        copy: {
            build: {
                files: [
                    //JS
                    { expand: true, cwd: 'components/requirejs/', src: ['require.js'], dest: '<%=jsVendorDir %>', filter: 'isFile' },
                    { expand: true, cwd: 'components/handlebars/', src: ['handlebars.runtime.js'], dest: '<%=jsVendorDir %>', filter: 'isFile' },
                    { expand: true, cwd: 'components/jquery/', src: ['jquery.min.js', 'jquery-migrate.min.js'], dest: '<%=jsVendorDir %>', filter: 'isFile' },
                    { expand: true, cwd: 'components/json3/lib/', src: ['json3.min.js'], dest: '<%=jsVendorDir %>', filter: 'isFile' },
                    { expand: true, cwd: 'components/knockout.js/', src: ['knockout.js'], dest: '<%=jsVendorDir %>', filter: 'isFile' },
                    { expand: true, cwd: 'components/sammy/lib/min/', src: ['sammy-latest.min.js'], dest: '<%=jsVendorDir %>', filter: 'isFile' },
                    { expand: true, cwd: 'components/zepto/', src: ['zepto.min.js'], dest: '<%=jsVendorDir %>', filter: 'isFile' },
                    { expand: true, cwd: 'components/modernizr/', src: ['modernizr.js'], dest: '<%=jsVendorDir %>', filter: 'isFile' },
                    { expand: true, cwd: 'components/underscore/', src: ['underscore-min.js'], dest: '<%=jsVendorDir %>', filter: 'isFile' },
                    //CSS
                    { expand: true, cwd: 'components/normalize-css/', src: ['normalize.css'], dest: '<%=cssDir %>', filter: 'isFile' }
                ]
            }
        },
        
        uglify: {
            options: {},
            build: {
                files: {
                    '<%=jsVendorDir %>require.min.js': ['<%=jsVendorDir %>require.js'],
                    '<%=jsVendorDir %>modernizr.dev.min.js': ['<%=jsVendorDir %>modernizr.js'],
                    '<%=jsVendorDir %>handlebars.runtime.min.js': ['<%=jsVendorDir %>handlebars.runtime.js']
                }
            }
        },
        
        watch: {
            sass: {
                files: ['<%= compassDir %>sass/**/*.scss'],
                tasks: ['compass:dev']
            }
        },
       
        /* Additional Properties */
        
        rootDir: 'public/',
        jsDir: '<%= rootDir %>js/',
        jsDistDir: '<%= jsDir %>dist/',
        jsVendorDir: '<%= jsDir %>vendor/',
        cssDir: '<%= rootDir %>css/',
        compassDir: 'compass/'
    });

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-compass');
    
    grunt.registerTask('build', ['copy:build', 'uglify:build']);
    
    grunt.registerTask('default', ['watch']);
};