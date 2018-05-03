module.exports = {
  entry: './src/site.ts',
  output: {
    filename: 'site.js'
  },
  resolve: {
    extensions: ['.js', '.ts', '.tsx']
  },
  module: {
    // rules: [{ test: /\.ts(x)?$/, use: { loader: "ts-loader" } }]
    rules: [{ test: /\.ts(x)?$/, loaders: ['babel-loader', 'ts-loader?silent=true'], exclude: /node_modules/ }]
    // loaders: [{ test: /\.ts(x)?$/, loaders: ['babel-loader', 'ts-loader?silent=true'], exclude: /node_modules/ }]
  },
  plugins: [],
  stats: { children: false },
  // externals: {
  //   'react': 'React',
  //   'react-dom': 'ReactDOM'
  // }
};
