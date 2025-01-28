const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
    transpileDependencies: true,

    publicPath: '/',
    outputDir: 'dist',
    assetsDir: 'assets',
    indexPath: 'index.html',
    css: {
        extract: true
    },

    devServer: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                logLevel: 'debug',
                ws: true,
            },
        },
    },
});
