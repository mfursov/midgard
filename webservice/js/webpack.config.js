const ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
    entry: './src/site.ts',
    output: {
        filename: 'site.js'
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
        new ExtractTextPlugin({filename: 'style.css', allChunks: true})
    ],
    externals: {
        'react': 'react',
        'react-dom': 'react-dom',
    },
    stats: {children: false}


};
