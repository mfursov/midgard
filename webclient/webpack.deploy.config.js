require("webpack");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require("path");

const dist = path.resolve(__dirname, "build/dist");

module.exports = {
    entry: {
        main: "main"
    },
    output: {
        filename: "[name].bundle.js",
        path: dist,
        publicPath: ""
    },
    module: {
        rules: [{
            test: /\.css$/,
            use: [
                'style-loader',
                'css-loader'
            ]
        }]
    },
    resolve: {
        modules: [
            path.resolve(__dirname, "node_modules"),
            path.resolve(__dirname, "node_modules/@jetbrains/kotlin-react/build/classes/main"),
            path.resolve(__dirname, "node_modules/@jetbrains/kotlin-react-dom/build/classes/main"),
            path.resolve(__dirname, "node_modules/@jetbrains/kotlin-extensions/build/classes/main"),
            path.resolve(__dirname, "build/resources/main/"),
            path.resolve(__dirname, "build/unpackKotlinJsStdlib/"),
            path.resolve(__dirname, "src/main/web/")
        ]
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Midgard Web Client'
        })
    ]
};
