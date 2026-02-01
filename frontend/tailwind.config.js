/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'amazon-orange': '#FF9900',
        'amazon-orange-hover': '#FA8900',
        'amazon-dark': '#131921',
        'amazon-light-dark': '#232F3E',
        'amazon-blue': '#146EB4',
        'amazon-yellow': '#FEBD69',
        'amazon-yellow-light': '#FEF8E7',
      },
    },
  },
  plugins: [],
}
