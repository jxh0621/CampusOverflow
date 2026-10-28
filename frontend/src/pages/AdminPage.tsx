import { useState } from 'react';
import { api } from '../api';
import { useAuth } from '../auth';

export function AdminPage() {
  const { me } = useAuth();
  const [tab, setTab] = useState<'verify' | 'staff'>('verify');
  const [users, setUsers] = useState<{ id: number; username: string; displayName: string; verified: boolean; role: string }[]>([]);
  const [staffForm, setStaffForm] = useState({ username: '', displayName: '', email: '', role: 'TEACHER', department: '' });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const loadUsers = async () => {
    try {
      const list = await api.adminUsers();
      setUsers(list);
    } catch (e: any) {
      setError(e.message);
    }
  };

  const verify = async (userId: number) => {
    try {
      await api.verifyUser(userId);
      setMsg('认证成功');
      loadUsers();
    } catch (e: any) {
      setError(e.message);
    }
  };

  const createStaff = async () => {
    try {
      await api.createStaff(staffForm);
      setMsg('教工账号已创建');
      setStaffForm({ username: '', displayName: '', email: '', role: 'TEACHER', department: '' });
    } catch (e: any) {
      setError(e.message);
    }
  };

  if (!me || (me.role !== 'ADMIN' && me.role !== 'TEACHER')) {
    return <p className="err">无权限访问管理页</p>;
  }

  return (
    <div className="layout">
      <section>
        <h1>管理后台</h1>
        <div className="tabs">
          <button className={tab === 'verify' ? 'active' : ''} onClick={() => { setTab('verify'); loadUsers(); }}>认证学生</button>
          <button className={tab === 'staff' ? 'active' : ''} onClick={() => setTab('staff')}>创建教工</button>
        </div>

        {tab === 'verify' && (
          <>
            <button onClick={loadUsers}>刷新列表</button>
            <table className="table">
              <thead><tr><th>学号</th><th>姓名</th><th>状态</th><th>操作</th></tr></thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.username}</td>
                    <td>{u.displayName}</td>
                    <td>{u.verified ? '已认证' : '未认证'}</td>
                    <td>{!u.verified && <button onClick={() => verify(u.id)}>认证</button>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        )}

        {tab === 'staff' && (
          <div className="form-page narrow">
            <label>工号<input value={staffForm.username} onChange={(e) => setStaffForm({ ...staffForm, username: e.target.value })} /></label>
            <label>姓名<input value={staffForm.displayName} onChange={(e) => setStaffForm({ ...staffForm, displayName: e.target.value })} /></label>
            <label>邮箱<input value={staffForm.email} onChange={(e) => setStaffForm({ ...staffForm, email: e.target.value })} /></label>
            <label>角色
              <select value={staffForm.role} onChange={(e) => setStaffForm({ ...staffForm, role: e.target.value })}>
                <option value="TEACHER">教师</option>
                <option value="TA">助教</option>
                <option value="ADMIN">管理员</option>
              </select>
            </label>
            <label>学院<input value={staffForm.department} onChange={(e) => setStaffForm({ ...staffForm, department: e.target.value })} /></label>
            <button className="primary" onClick={createStaff}>创建账号</button>
          </div>
        )}

        {msg && <p className="ok">{msg}</p>}
        {error && <p className="err">{error}</p>}
      </section>
    </div>
  );
}
