export type AuditAction = 
  | 'CREATE' 
  | 'UPDATE' 
  | 'DELETE' 
  | 'ASSIGN' 
  | 'RETURN' 
  | 'STATUS_CHANGE' 
  | 'LOGIN' 
  | 'LOGOUT' 
  | 'TOKEN_REFRESH';

export interface AuditLogResponse {
  id: number;
  userId?: number | null;
  usernameSnapshot?: string | null;
  entityName: string;
  entityId?: number | null;
  action: AuditAction;
  oldValue?: Record<string, any> | string | null;
  newValue?: Record<string, any> | string | null;
  ipAddress?: string | null;
  createdAt: string;
}
