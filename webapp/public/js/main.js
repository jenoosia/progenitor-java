//Possible issues using Zepto with jQuery plugins?
function useZepto() { return '__proto__' in {}; } 

/*
 * For multi-page applications, this file will serve as the common configuration point for requireJS. Custom JS to be execute for
 * each page should either reside in a different script tag/file or be included here but as part of a conditional statement.
 */

require.config({
    paths: {
        jquery: useZepto() ? 'vendor/zepto.min' : ['//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min', 'vendor/jquery.min'],
        json3: 'vendor/json3.min',
        underscore: 'vendor/underscore-min',
        knockout: 'vendor/knockout',
        koClassBindingProvider: 'dist/knockout-classBindingProvider.min'
    },
    shim: {
        underscore: {
            exports: '_'
        },
        jquery: {
            exports: '$'
        },
        json3: {
            exports: 'JSON'
        }
    }
})

require(['jquery'], function($) {
    
    'use strict';
    
    $.support.cors = true; //Support cross-origin request sharing
    
    // sample usage (more here: https://github.com/rniemeyer/knockout-classBindingProvider)
    // ko.bindingProvider.instance = new ClassBindingProvider(bindings);
    
    //TODO App entrypoint
    $(document).ready(function() {
    });
    
});
