variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "project" {
  type    = string
  default = "telecom-churn-intel"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "ssh_public_key" {
  type = string
}

variable "gemini_api_key" {
  type      = string
  sensitive = true
}

variable "mongo_uri" {
  description = "MongoDB Atlas connection string (leave empty to use local Docker MongoDB)"
  type        = string
  sensitive   = true
  default     = ""
}

variable "db_password" {
  description = "MongoDB password"
  type        = string
  sensitive   = true
  default     = "Admin@123"
}

variable "repo_url" {
  description = "GitHub repo HTTPS URL to clone on EC2"
  type        = string
  default     = "https://github.com/<your-username>/Telecom-Churn-Intelligence.git"
}

variable "fallback_ip" {
  type    = string
  default = ""
}
