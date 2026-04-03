# Portfolio API Manifest

## Overview
This document provides a comprehensive summary of all REST API endpoints in the Spring Boot Portfolio application. Each endpoint includes the HTTP method, URL, request/response structures, and authentication requirements.

---

## Authentication
- **Public Endpoints**: No authentication required
- **Admin Restricted**: Requires Bearer Token in the `Authorization` header
  - Format: `Authorization: Bearer {token}`

---

## Endpoints by Controller

### 1. AUTH CONTROLLER (`/api/v1/auth`)

#### 1.1 Login
- **HTTP Method**: `POST`
- **URL**: `/api/v1/auth/login`
- **Authentication**: Public (No Bearer Token)
- **Description**: Authenticates user and returns a JWT token

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response Body** (Status: 200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0..."
}
```

---

### 2. PROFILE CONTROLLER (`/api/v1/profile`)

#### 2.1 Get Profile Data
- **HTTP Method**: `GET`
- **URL**: `/api/v1/profile/getProfile`
- **Authentication**: Public (No Bearer Token)
- **Description**: Retrieves hero section data (name, headline, bio, resume URL)

**Request Body**: None

**Response Body** (Status: 200 OK):
```json
{
  "name": "John Doe",
  "headline": "Senior Full Stack Developer",
  "subHeadline": "Passionate about building scalable web applications with modern tech stack",
  "resumeUrl": "https://cloudinary.com/resume.pdf"
}
```

#### 2.2 Download Resume
- **HTTP Method**: `GET`
- **URL**: `/api/v1/profile/resume/download`
- **Authentication**: Public (No Bearer Token)
- **Description**: Tracks resume download and redirects to cloudinary URL

**Request Body**: None

**Response**: 
- Status: `302 Found` (Redirect)
- Location Header: `{cloudinary_resume_url}`
- Logs the download with user's IP address

#### 2.3 Get Download Statistics
- **HTTP Method**: `GET`
- **URL**: `/api/v1/profile/resume/stats`
- **Authentication**: Public (No Bearer Token)
- **Description**: Retrieves download statistics for the resume

**Request Body**: None

**Response Body** (Status: 200 OK):
```json
{
  "downloads": [
    {
      "id": 1,
      "ipAddress": "192.168.1.1",
      "downloadedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": 2,
      "ipAddress": "192.168.1.2",
      "downloadedAt": "2024-01-16T14:22:00Z"
    }
  ]
}
```

---

### 3. ABOUT CONTROLLER (`/api/v1/about`)

#### 3.1 Get About Data
- **HTTP Method**: `GET`
- **URL**: `/api/v1/about`
- **Authentication**: Public (No Bearer Token)
- **Description**: Retrieves complete About section including left panel and tabs (skills, experience, awards, certifications)

**Request Body**: None

**Response Body** (Status: 200 OK):
```json
{
  "leftSection": {
    "title": "About Me",
    "description": "I am a passionate developer with 5+ years of experience",
    "yearsExperience": "5+",
    "projectsCompleted": "20+",
    "techDebtReduced": "30%"
  },
  "tabs": [
    {
      "title": "skills",
      "info": [
        {
          "title": "Frontend & UI",
          "icons": [
            {
              "icon": "react",
              "title": "React"
            },
            {
              "icon": "vue",
              "title": "Vue.js"
            }
          ]
        }
      ]
    },
    {
      "title": "experience",
      "info": [
        {
          "title": "Senior Developer",
          "stage": "2022 - Present",
          "description": "Leading frontend development team"
        }
      ]
    },
    {
      "title": "awards",
      "info": [
        {
          "title": "Best Developer Award",
          "stage": "2023",
          "description": "Recognized for outstanding contributions"
        }
      ]
    },
    {
      "title": "certifications",
      "info": [
        {
          "title": "AWS Solutions Architect",
          "stage": "2022",
          "description": "Professional certification from AWS"
        }
      ]
    }
  ]
}
```

---

### 4. WORK CONTROLLER (`/api/v1/work`)

#### 4.1 Get Work Section Data
- **HTTP Method**: `GET`
- **URL**: `/api/v1/work`
- **Authentication**: Public (No Bearer Token)
- **Description**: Retrieves featured projects/engineering work section

**Request Body**: None

**Response Body** (Status: 200 OK):
```json
{
  "title": "Featured Projects",
  "description": "Here are some of my recent engineering projects",
  "projects": [
    {
      "title": "E-Commerce Platform",
      "path": "/project/ecommerce",
      "techStack": ["React", "Node.js", "MongoDB"],
      "description": "Full-stack e-commerce solution with payment integration",
      "ctaLinks": [
        {
          "title": "View Live",
          "icon": "external-link",
          "url": "https://ecommerce-demo.com"
        },
        {
          "title": "GitHub",
          "icon": "github",
          "url": "https://github.com/user/ecommerce"
        }
      ]
    },
    {
      "title": "Task Management App",
      "path": "/project/taskapp",
      "techStack": ["Vue.js", "Django", "PostgreSQL"],
      "description": "Collaborative task management with real-time updates",
      "ctaLinks": [
        {
          "title": "View Live",
          "icon": "external-link",
          "url": "https://taskapp-demo.com"
        }
      ]
    }
  ]
}
```

---

### 5. CONTACT CONTROLLER (`/api/v1/contact`)

#### 5.1 Send Contact Message
- **HTTP Method**: `POST`
- **URL**: `/api/v1/contact`
- **Authentication**: Public (No Bearer Token)
- **Description**: Sends a contact message from the contact form

**Request Body**:
```json
{
  "name": "Jane Smith",
  "email": "jane@example.com",
  "subject": "Project Inquiry",
  "message": "I would like to discuss a collaboration opportunity for your expertise in React development."
}
```

**Response Body** (Status: 200 OK):
```json
{
  "message": "Talk to you soon, Ajay!"
}
```

**Validation Rules**:
- `name`: Required, non-blank
- `email`: Required, valid email format
- `subject`: Optional
- `message`: Required, minimum 10 characters

---

### 6. ADMIN CONTROLLER (`/api/v1/admin`)

⚠️ **All endpoints require Bearer Token authentication (Admin Restricted)**

#### 6.1 Update Profile
- **HTTP Method**: `PUT`
- **URL**: `/api/v1/admin/profile`
- **Authentication**: Required - Bearer Token
- **Description**: Updates the main profile/hero section (ID is always 1)

**Request Body**:
```json
{
  "id": 1,
  "name": "John Doe",
  "headline": "Senior Full Stack Developer",
  "subHeadline": "Passionate about building scalable web applications with modern tech stack",
  "resumeUrl": "https://cloudinary.com/resume.pdf"
}
```

**Response Body** (Status: 200 OK):
```json
{
  "id": 1,
  "name": "John Doe",
  "headline": "Senior Full Stack Developer",
  "subHeadline": "Passionate about building scalable web applications with modern tech stack",
  "resumeUrl": "https://cloudinary.com/resume.pdf"
}
```

#### 6.2 Update About Section
- **HTTP Method**: `PUT`
- **URL**: `/api/v1/admin/about/section`
- **Authentication**: Required - Bearer Token
- **Description**: Updates the about section data (ID is always 1)

**Request Body**:
```json
{
  "id": 1,
  "title": "About Me",
  "description": "I am a passionate developer with 5+ years of experience",
  "yearsExperience": "5+",
  "projectsCompleted": "20+",
  "techDebtReduced": "30%"
}
```

**Response Body** (Status: 200 OK):
```json
{
  "id": 1,
  "title": "About Me",
  "description": "I am a passionate developer with 5+ years of experience",
  "yearsExperience": "5+",
  "projectsCompleted": "20+",
  "techDebtReduced": "30%"
}
```

#### 6.3 Add Experience
- **HTTP Method**: `POST`
- **URL**: `/api/v1/admin/experience`
- **Authentication**: Required - Bearer Token
- **Description**: Creates a new experience entry

**Request Body**:
```json
{
  "title": "Senior Developer",
  "stage": "2022 - Present",
  "description": "Leading frontend development team at Tech Company"
}
```

**Response Body** (Status: 200 OK):
```json
{
  "id": 1,
  "title": "Senior Developer",
  "stage": "2022 - Present",
  "description": "Leading frontend development team at Tech Company"
}
```

#### 6.4 Delete Experience
- **HTTP Method**: `DELETE`
- **URL**: `/api/v1/admin/experience/{id}`
- **Authentication**: Required - Bearer Token
- **Description**: Deletes an experience entry by ID

**Request Body**: None

**Response**: Status: 204 No Content

#### 6.5 Add Project
- **HTTP Method**: `POST`
- **URL**: `/api/v1/admin/projects`
- **Authentication**: Required - Bearer Token
- **Description**: Creates a new project

**Request Body**:
```json
{
  "title": "E-Commerce Platform",
  "thumbnailPath": "/images/ecommerce.jpg",
  "description": "Full-stack e-commerce solution with payment integration",
  "techStack": ["React", "Node.js", "MongoDB"],
  "ctaLinks": [
    {
      "label": "View Live",
      "iconName": "external-link",
      "url": "https://ecommerce-demo.com"
    },
    {
      "label": "GitHub",
      "iconName": "github",
      "url": "https://github.com/user/ecommerce"
    }
  ]
}
```

**Response Body** (Status: 200 OK):
```json
{
  "id": 1,
  "title": "E-Commerce Platform",
  "thumbnailPath": "/images/ecommerce.jpg",
  "description": "Full-stack e-commerce solution with payment integration",
  "techStack": ["React", "Node.js", "MongoDB"],
  "ctaLinks": [
    {
      "id": 1,
      "label": "View Live",
      "iconName": "external-link",
      "url": "https://ecommerce-demo.com"
    },
    {
      "id": 2,
      "label": "GitHub",
      "iconName": "github",
      "url": "https://github.com/user/ecommerce"
    }
  ]
}
```

#### 6.6 Delete Project
- **HTTP Method**: `DELETE`
- **URL**: `/api/v1/admin/projects/{id}`
- **Authentication**: Required - Bearer Token
- **Description**: Deletes a project by ID

**Request Body**: None

**Response**: Status: 204 No Content

#### 6.7 Add Award
- **HTTP Method**: `POST`
- **URL**: `/api/v1/admin/awards`
- **Authentication**: Required - Bearer Token
- **Description**: Creates a new award entry

**Request Body**:
```json
{
  "title": "Best Developer Award",
  "stage": "2023",
  "description": "Recognized for outstanding contributions to the team"
}
```

**Response Body** (Status: 200 OK):
```json
{
  "id": 1,
  "title": "Best Developer Award",
  "stage": "2023",
  "description": "Recognized for outstanding contributions to the team"
}
```

#### 6.8 Delete Award
- **HTTP Method**: `DELETE`
- **URL**: `/api/v1/admin/awards/{id}`
- **Authentication**: Required - Bearer Token
- **Description**: Deletes an award by ID

**Request Body**: None

**Response**: Status: 204 No Content

#### 6.9 Add Certification
- **HTTP Method**: `POST`
- **URL**: `/api/v1/admin/certifications`
- **Authentication**: Required - Bearer Token
- **Description**: Creates a new certification entry

**Request Body**:
```json
{
  "title": "AWS Solutions Architect",
  "stage": "2022",
  "description": "Professional certification from AWS"
}
```

**Response Body** (Status: 200 OK):
```json
{
  "id": 1,
  "title": "AWS Solutions Architect",
  "stage": "2022",
  "description": "Professional certification from AWS"
}
```

#### 6.10 Delete Certification
- **HTTP Method**: `DELETE`
- **URL**: `/api/v1/admin/certifications/{id}`
- **Authentication**: Required - Bearer Token
- **Description**: Deletes a certification by ID

**Request Body**: None

**Response**: Status: 204 No Content

---

## Summary Table

| Controller | Endpoint | Method | Auth | Description |
|-----------|----------|--------|------|-------------|
| Auth | `/api/v1/auth/login` | POST | Public | Login and get JWT token |
| Profile | `/api/v1/profile/getProfile` | GET | Public | Get hero section data |
| Profile | `/api/v1/profile/resume/download` | GET | Public | Download resume (tracked) |
| Profile | `/api/v1/profile/resume/stats` | GET | Public | Get resume download stats |
| About | `/api/v1/about` | GET | Public | Get full about section data |
| Work | `/api/v1/work` | GET | Public | Get featured projects |
| Contact | `/api/v1/contact` | POST | Public | Send contact message |
| Admin | `/api/v1/admin/profile` | PUT | Bearer Token | Update profile |
| Admin | `/api/v1/admin/about/section` | PUT | Bearer Token | Update about section |
| Admin | `/api/v1/admin/experience` | POST | Bearer Token | Add experience |
| Admin | `/api/v1/admin/experience/{id}` | DELETE | Bearer Token | Delete experience |
| Admin | `/api/v1/admin/projects` | POST | Bearer Token | Add project |
| Admin | `/api/v1/admin/projects/{id}` | DELETE | Bearer Token | Delete project |
| Admin | `/api/v1/admin/awards` | POST | Bearer Token | Add award |
| Admin | `/api/v1/admin/awards/{id}` | DELETE | Bearer Token | Delete award |
| Admin | `/api/v1/admin/certifications` | POST | Bearer Token | Add certification |
| Admin | `/api/v1/admin/certifications/{id}` | DELETE | Bearer Token | Delete certification |

---

## Error Handling

All endpoints follow REST conventions for error responses:

- **400 Bad Request**: Invalid input or validation failure
- **401 Unauthorized**: Missing or invalid Bearer Token for protected endpoints
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side error

---

## Notes

1. **Admin Endpoints**: All `/api/v1/admin/*` endpoints require a valid JWT token obtained from the `/api/v1/auth/login` endpoint
2. **ID Management**: 
   - Profile and About Section always have ID = 1 (singleton resources)
   - Experience, Projects, Awards, and Certifications are auto-generated with ID
3. **Soft Dependencies**: Deleting projects will cascade delete associated project links
4. **Validation**: Contact form requires email validation and minimum message length

---

**Last Updated**: April 2024
**API Version**: v1

