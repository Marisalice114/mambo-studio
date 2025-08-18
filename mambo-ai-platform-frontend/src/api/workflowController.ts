// @ts-ignore
/* eslint-disable */
import request from '@/request';

/** 同步执行工作流 POST /workflow/execute */
export async function executeWorkflow(
  params: {
    /** prompt */
    prompt: string;
  },
  options?: { [key: string]: any },
) {
  return request<any>('/workflow/execute', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** Flux 流式执行工作流 GET /workflow/execute-flux */
export async function executeWorkflowWithFlux(
  params: {
    /** prompt */
    prompt: string;
  },
  options?: { [key: string]: any },
) {
  return request<string>('/workflow/execute-flux', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** SSE 流式执行工作流 GET /workflow/execute-sse */
export async function executeWorkflowWithSse(
  params: {
    /** prompt */
    prompt: string;
  },
  options?: { [key: string]: any },
) {
  return request<any>('/workflow/execute-sse', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
