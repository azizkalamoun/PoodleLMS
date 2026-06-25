export const environment = {
  production: true,
  apiUrl: 'https://backend-springboot-i7r7.onrender.com/api',

  cloudinary: {
    cloudName: 'dnmhrecwb',
    uploadPreset: 'poodle_lms',
    apiUrl: 'https://api.cloudinary.com/v1_1/dnmhrecwb/image/upload',
    // Optional: API credentials for asset deletion (use environment variables in production)
    // WARNING: Never commit API secrets to source control!
    // Use build-time substitution or secret management
    apiKey: '557169748541557', // Load from environment variable: process.env['CLOUDINARY_API_KEY']
    apiSecret: 'OTD40x5ptkIjtN3LmabYrA5Mwf8', // Load from environment variable: process.env['CLOUDINARY_API_SECRET']
  },
};
