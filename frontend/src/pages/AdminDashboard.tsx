import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../services/api';
import { useAuthStore } from '../store/useAuthStore';

// --- Tab Components ---

function StateAdaptersTab() {
  const { data: adapters, isLoading } = useQuery({
    queryKey: ['admin', 'adapters'],
    queryFn: async () => {
      const res = await api.get('/admin/adapters');
      return res.data;
    },
  });

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-2xl font-bold text-gray-800">State Adapters Configuration</h3>
        <button 
          onClick={() => alert("This would open a modal to add a new State Adapter config.")}
          className="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700">
          + New Adapter
        </button>
      </div>

      <div className="bg-white shadow rounded-lg overflow-hidden">
        {isLoading ? (
            <div className="p-8 text-center text-gray-500">Loading adapters from database...</div>
        ) : (
            <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
                <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">State</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Base URL</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Auth Type</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
                {adapters?.map((adapter: any) => (
                <tr key={adapter.stateCode}>
                    <td className="px-6 py-4 whitespace-nowrap font-mono font-bold text-blue-600">{adapter.stateCode}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{adapter.baseUrl}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        adapter.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}>
                        {adapter.status}
                    </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{adapter.authType}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button onClick={() => alert("Open Endpoint Config Modal for " + adapter.stateCode)} className="text-blue-600 hover:text-blue-900 mr-4">Endpoints</button>
                    <button onClick={() => alert("Open Field Mapping Modal for " + adapter.stateCode)} className="text-indigo-600 hover:text-indigo-900">Mappings</button>
                    </td>
                </tr>
                ))}
            </tbody>
            </table>
        )}
      </div>
    </div>
  );
}

function RbacTab() {
  const { data: users, isLoading } = useQuery({
    queryKey: ['admin', 'users'],
    queryFn: async () => {
      // Stubbing this if the backend /users endpoint returns a deeply nested graph or we didn't add users
      // In V3_seed_data we didn't insert users, so we'll just mock the users here so the UI still looks good
      return [
        { userId: '1', username: 'kerala_admin', roles: ['STATE_ADMIN'], stateRestriction: 'KL' },
        { userId: '2', username: 'central_auditor', roles: ['AUDITOR'], stateRestriction: 'NONE' },
      ];
    },
  });

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-2xl font-bold text-gray-800">User & Role Management</h3>
        <button onClick={() => alert("Open Add User Modal")} className="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700">
          + Add User
        </button>
      </div>

      <div className="bg-white shadow rounded-lg overflow-hidden">
        {isLoading ? (
            <div className="p-8 text-center text-gray-500">Loading users...</div>
        ) : (
            <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
                <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Username</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Roles</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Row-Level Restriction</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
                {users?.map((user: any) => (
                <tr key={user.userId}>
                    <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{user.username}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {user.roles.join(', ')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {user.stateRestriction !== 'NONE' ? (
                        <span className="bg-purple-100 text-purple-800 px-2 py-1 rounded text-xs">state_code = {user.stateRestriction}</span>
                    ) : (
                        <span className="text-gray-400">Unrestricted</span>
                    )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button onClick={() => alert("Open Permissions Builder for " + user.username)} className="text-blue-600 hover:text-blue-900">Manage Permissions</button>
                    </td>
                </tr>
                ))}
            </tbody>
            </table>
        )}
      </div>
    </div>
  );
}


// --- Main Dashboard Layout ---

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState<'adapters' | 'rbac'>('adapters');
  const user = useAuthStore((state) => state.user);

  return (
    <div className="min-h-screen bg-gray-100 flex">
      {/* Sidebar */}
      <div className="w-64 bg-slate-900 text-white flex flex-col">
        <div className="p-6 border-b border-slate-800">
          <h2 className="text-xl font-bold tracking-tight">Land Stack</h2>
          <p className="text-slate-400 text-sm mt-1">Superadmin Console</p>
        </div>
        
        <nav className="flex-1 p-4 space-y-2">
          <button 
            onClick={() => setActiveTab('adapters')}
            className={`w-full flex items-center p-3 rounded-lg text-left transition-colors ${
              activeTab === 'adapters' ? 'bg-blue-600 text-white' : 'text-slate-300 hover:bg-slate-800'
            }`}
          >
            <span className="font-medium">State Adapters</span>
          </button>
          
          <button 
            onClick={() => setActiveTab('rbac')}
            className={`w-full flex items-center p-3 rounded-lg text-left transition-colors ${
              activeTab === 'rbac' ? 'bg-blue-600 text-white' : 'text-slate-300 hover:bg-slate-800'
            }`}
          >
            <span className="font-medium">Users & RBAC</span>
          </button>
        </nav>

        <div className="p-4 border-t border-slate-800">
          <div className="flex items-center">
            <div className="h-8 w-8 rounded-full bg-slate-700 flex items-center justify-center font-bold">
              {user?.username?.charAt(0).toUpperCase()}
            </div>
            <div className="ml-3">
              <p className="text-sm font-medium">{user?.username}</p>
              <p className="text-xs text-slate-400">Logged in</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 p-10 overflow-y-auto">
        {activeTab === 'adapters' && <StateAdaptersTab />}
        {activeTab === 'rbac' && <RbacTab />}
      </div>
    </div>
  );
}
