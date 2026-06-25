export interface Department {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  parentName?: string;
  children?: Department[];
  employeeCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface DepartmentCreateRequest {
  name: string;
  description?: string;
  parentId?: number;
}
