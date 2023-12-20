/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {Injectable} from "@angular/core";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ExecutionFailedState} from "./ExecutionFailedState";
import {CommandResult} from "./CommandResult";
import {ApplyRetrySpec} from "./ApplyRetrySpec";
import {PagedQuery} from "./PagedQuery";
import {PagedList} from "./PagedList";

@Injectable({providedIn: 'root'})
export class CompensationClient {
  aggregateName = 'execution_failed';
  commandApi = environment.host + `/${this.aggregateName}`;
  retryApi = environment.host + '/failed';

  constructor(private httpClient: HttpClient) {

  }

  prepare(id: string): Observable<CommandResult> {
    const apiUrl = `${this.commandApi}/${id}/prepare_compensation`;
    return this.httpClient.put<CommandResult>(apiUrl, {});
  }

  applyRetrySpec(id: string, appRetrySpec: ApplyRetrySpec): Observable<CommandResult> {
    const apiUrl = `${this.commandApi}/${id}/apply_retry_spec`;
    return this.httpClient.put<CommandResult>(apiUrl, appRetrySpec);
  }

  findAll(pagedQuery: PagedQuery): Observable<PagedList<ExecutionFailedState>> {
    const apiUrl = `${this.retryApi}/all`;
    return this.httpClient.post<PagedList<ExecutionFailedState>>(apiUrl, pagedQuery);
  }

  findNextRetry(pagedQuery: PagedQuery): Observable<PagedList<ExecutionFailedState>> {
    const apiUrl = `${this.retryApi}/next-retry`;
    return this.httpClient.post<PagedList<ExecutionFailedState>>(apiUrl, pagedQuery);
  }

  findToRetry(pagedQuery: PagedQuery): Observable<PagedList<ExecutionFailedState>> {
    const apiUrl = `${this.retryApi}/to-retry`;
    return this.httpClient.post<PagedList<ExecutionFailedState>>(apiUrl, pagedQuery);
  }

  findNonRetryable(pagedQuery: PagedQuery): Observable<PagedList<ExecutionFailedState>> {
    const apiUrl = `${this.retryApi}/non-retryable`;
    return this.httpClient.post<PagedList<ExecutionFailedState>>(apiUrl, pagedQuery);
  }

  findSuccess(pagedQuery: PagedQuery): Observable<PagedList<ExecutionFailedState>> {
    const apiUrl = `${this.retryApi}/success`;
    return this.httpClient.post<PagedList<ExecutionFailedState>>(apiUrl, pagedQuery);
  }
}
