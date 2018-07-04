const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
    entry: './src/site.ts',
    output: {
        path: __dirname + '/build/dist',
        filename: 'js/webclient.js'
    },
    resolve: {
        extensions: ['.js', '.ts', '.tsx']
    },
    module: {
        rules: [
            {test: /\.ts(x)?$/, loaders: ['babel-loader', 'ts-loader?silent=true'], exclude: /node_modules/},
            {test: /\.css$/, loader: ExtractTextPlugin.extract('css-loader')}
        ]
    },
    plugins: [
        new ExtractTextPlugin({
            filename: 'css/webclient.css',
            allChunks: true
        }),
        new CopyWebpackPlugin([
            // {from: 'resources'}
        ])
    ],
    externals: {},
    stats: {children: false}
};
