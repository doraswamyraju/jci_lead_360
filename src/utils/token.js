import jwt from 'jsonwebtoken';

const JWT_SECRET = process.env.JWT_SECRET || 'jci_lead360_secure_jwt_token_key_2027';

export function generateToken(user) {
  return jwt.sign(
    {
      id: user.id,
      email: user.email,
      role: user.role,
      name: user.name,
      chapterId: user.chapterId || null,
      chapterName: user.chapterName || null
    },
    JWT_SECRET,
    { expiresIn: '30d' }
  );
}

export function verifyToken(token) {
  try {
    return jwt.verify(token, JWT_SECRET);
  } catch (error) {
    return null;
  }
}
